const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = "demo-no-project";
const TEACHER_UID = "teacher_123";
const TEACHER_EMAIL = "teacher@tewahedo.org";
const STUDENT_UID = "student_456";
const STUDENT_EMAIL = "student@tewahedo.org";
const OTHER_STUDENT_EMAIL = "other@tewahedo.org";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user is denied reading members or grades", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("church_members").doc("MH-001").get());
  await assertFails(unauthDb.collection("church_grades").doc("GR-001").get());
});

test("Teacher can write attendance and grades", async () => {
  const teacherDb = testEnv.authenticatedContext(TEACHER_UID, {
    email: TEACHER_EMAIL,
    role: "Teacher"
  }).firestore();

  await assertSucceeds(
    teacherDb.collection("church_grades").doc("GR-001").set({
      studentId: "MH-001",
      studentEmail: STUDENT_EMAIL,
      studentName: "Abebe Kebede",
      subject: "Orthodox Dogma",
      term: "Term 1",
      score: 95,
      grade: "A",
      teacherName: "Yared Teacher",
      date: "2024-10-08"
    })
  );

  await assertSucceeds(
    teacherDb.collection("church_attendance").doc("ATT-001").set({
      memberId: "MH-001",
      email: STUDENT_EMAIL,
      memberName: "Abebe Kebede",
      date: "2024-10-06",
      serviceType: "Sunday School Class",
      status: "Present",
      recordedBy: "Yared Teacher"
    })
  );
});

test("Student cannot write or modify grades or attendance", async () => {
  const studentDb = testEnv.authenticatedContext(STUDENT_UID, {
    email: STUDENT_EMAIL,
    role: "Student"
  }).firestore();

  await assertFails(
    studentDb.collection("church_grades").doc("GR-002").set({
      studentId: "MH-001",
      studentEmail: STUDENT_EMAIL,
      subject: "Orthodox Dogma",
      score: 100
    })
  );

  await assertFails(
    studentDb.collection("church_attendance").doc("ATT-002").set({
      memberId: "MH-001",
      date: "2024-10-06",
      status: "Present"
    })
  );
});

test("Student can read their own member record and grades, but not other students", async () => {
  // Preload records via admin/teacher
  const teacherDb = testEnv.authenticatedContext(TEACHER_UID, {
    email: TEACHER_EMAIL,
    role: "Teacher"
  }).firestore();

  await teacherDb.collection("church_members").doc("MH-001").set({
    memberId: "MH-001",
    fullName: "Abebe Kebede",
    email: STUDENT_EMAIL,
    phone: "+251911111111",
    classGrade: "Grade 9"
  });

  await teacherDb.collection("church_members").doc("MH-002").set({
    memberId: "MH-002",
    fullName: "Other Student",
    email: OTHER_STUDENT_EMAIL,
    phone: "+251922222222",
    classGrade: "Grade 9"
  });

  await teacherDb.collection("church_grades").doc("GR-001").set({
    studentId: "MH-001",
    studentEmail: STUDENT_EMAIL,
    studentName: "Abebe Kebede",
    subject: "Hymns",
    score: 92
  });

  const studentDb = testEnv.authenticatedContext(STUDENT_UID, {
    email: STUDENT_EMAIL,
    role: "Student"
  }).firestore();

  // Student reading own member record -> SUCCEEDS
  await assertSucceeds(studentDb.collection("church_members").doc("MH-001").get());

  // Student reading own grade -> SUCCEEDS
  await assertSucceeds(studentDb.collection("church_grades").doc("GR-001").get());

  // Student reading other student's member record -> FAILS
  await assertFails(studentDb.collection("church_members").doc("MH-002").get());
});

test("Manager/Admin can assign roles in church_users", async () => {
  const managerDb = testEnv.authenticatedContext("manager_1", {
    email: "manager@tewahedo.org",
    role: "Manager"
  }).firestore();

  await assertSucceeds(
    managerDb.collection("church_users").doc("newuser@tewahedo.org").set({
      id: 101,
      email: "newuser@tewahedo.org",
      fullName: "New Teacher",
      phone: "+251911000000",
      role: "Teacher",
      departmentId: 2,
      isActive: true,
      isApproved: true,
      approvalStatus: "APPROVED"
    })
  );
});

test("Teacher can post weekly lesson note, student can read but cannot post", async () => {
  const teacherDb = testEnv.authenticatedContext(TEACHER_UID, {
    email: TEACHER_EMAIL,
    role: "Teacher"
  }).firestore();

  // Teacher posting lesson -> SUCCEEDS
  await assertSucceeds(
    teacherDb.collection("church_lessons").doc("LESSON-001").set({
      lessonId: "LESSON-001",
      title: "Orthodox Dogma: Holy Trinity",
      subject: "Orthodox Dogma",
      classGrade: "Grade 9-12",
      weekNumber: 1,
      datePosted: "2024-10-06",
      summary: "Detailed overview of the Holy Trinity in Ethiopian Orthodox Tewahedo Church.",
      teacherName: "Memhir Dereje",
      teacherEmail: TEACHER_EMAIL
    })
  );

  const studentDb = testEnv.authenticatedContext(STUDENT_UID, {
    email: STUDENT_EMAIL,
    role: "Student"
  }).firestore();

  // Student reading lesson -> SUCCEEDS
  await assertSucceeds(studentDb.collection("church_lessons").doc("LESSON-001").get());

  // Student trying to post lesson -> FAILS
  await assertFails(
    studentDb.collection("church_lessons").doc("LESSON-002").set({
      lessonId: "LESSON-002",
      title: "Student Post",
      subject: "Bible Study",
      summary: "Unauthorized student summary"
    })
  );
});
