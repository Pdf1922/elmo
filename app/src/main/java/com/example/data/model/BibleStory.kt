package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bible_stories")
data class BibleStory(
  @PrimaryKey val id: String,
  val dayNumber: Int,
  val titleAmharic: String,
  val titleEnglish: String,
  val verseReference: String,
  val verseTextAmharic: String,
  val verseTextEnglish: String,
  val storyAmharic: String,
  val storyEnglish: String,
  val moralAmharic: String,
  val moralEnglish: String,
  val prayerAmharic: String,
  val prayerEnglish: String,
  val readingTimeMinutes: Int = 3,
  val liturgicalConnection: String = "Meserete Hiwot Sunday School"
)

object BibleStoryRepository {
  val dailyStories: List<BibleStory> = listOf(
    BibleStory(
      id = "story_1",
      dayNumber = 1,
      titleAmharic = "ሊቀ መላእክት ቅዱስ ገብርኤልና ሠለስቱ ደቂቅ በእቶነ እሳት",
      titleEnglish = "Archangel St. Gabriel & the Three Holy Children in the Fiery Furnace",
      verseReference = "ዳንኤል 3፥28 • Daniel 3:28",
      verseTextAmharic = "«የሲድራቅንና የሚሳቅን የአብደናጎንም አምላክ ይባረክ፤ በእርሱ የታመኑትን አገልጋዮቹን መልአኩን ልኮ አዳናቸው።»",
      verseTextEnglish = "«Blessed be the God of Shadrach, Meshach, and Abednego, who sent His Angel and delivered His servants who trusted in Him.»",
      storyAmharic = """
        በባቢሎን ንጉሥ በናቡከደነፆር ዘመን ሦስቱ ወጣቶች (ሲድራቅ፣ ሚሳቅና አብደናጎ - አናንያ፣ አዛርያና ሚሳኤል) ለወርቁ ምስል እንዳይሰግዱ በእግዚአብሔር ላይ ጸኑ። ንጉሡም እጅግ ተቆጥቶ እሳቱን ሰባት እጥፍ አበረታታ። ወደ እቶኑ እሳት ሲጣሉም ቅዱስ ገብርኤል ፈጥኖ ወረደ፤ እሳቱን እንደ ቀዝቃዛ ጤዛና ነፋስ አደረገው፤ ፀጉራቸው እንኳ ሳይቃጠል አዳናቸው። 

        ንጉሡም ሲመለከት አራት ሰዎች በእሳቱ መካከል ሲመላለሱ አየ፤ የአራተኛውም መልክ የእግዚአብሔርን ልጅ ይመስል ነበር። በደብረ ኃይል ቤተክርስቲያናችን ቅዱስ ገብርኤል በታላቅ ክብር የሚወደሰውም ለዚህ የድነት ተልእኮው ነው።
      """.trimIndent(),
      storyEnglish = """
        During the reign of King Nebuchadnezzar in Babylon, three faithful Hebrew youths (Ananias, Azarias, and Misael) refused to bow to the golden idol, remaining steadfast in the God of Israel. In fury, the king commanded the furnace to be heated seven times hotter than usual and cast them bound into the roaring flames.

        Suddenly, the Holy Archangel St. Gabriel descended into the midst of the flames, driving the fire away and making the furnace like a cool, dew-laden breeze. When the king peered inside, he marvelled to see four men walking unbound and untouched by the fire, the form of the fourth resembling the Son of God. At our Bole Arabsa Debre Hayil Church, St. Gabriel is venerated with deep joy for his unfailing intercession.
      """.trimIndent(),
      moralAmharic = "በፈተና እና በእሳት መከራ መካከል እንኳ እግዚአብሔር አገልጋዮቹን ፈጽሞ አይጥላቸውም። እምነታችንን ስንጠብቅ መልአኩ ቅዱስ ገብርኤል ለነፍሳችን ሰላምን ያመጣልናል።",
      moralEnglish = "No fire or trial in this world can harm those who abide faithfully under God's grace. Our patron Archangel St. Gabriel remains an ever-present protector.",
      prayerAmharic = "አቤቱ አምላካችን ሆይ! በቅዱስ ገብርኤል ተራዳኢነት ከኃጢአትና ከፈተና እሳት ሁሉ ጠብቀን፤ በሰንበት ትምህርት ቤታችንም ጸንተን እንድናገለግልህ ቅዱስ ፈቃድህ ይሁንልን። አሜን።",
      prayerEnglish = "O Lord God of our fathers, through the prayers of the Holy Archangel Gabriel, deliver us from every fiery trial and flame of temptation. Keep our Sunday School youth faithful forever. Amen."
    ),
    BibleStory(
      id = "story_2",
      dayNumber = 2,
      titleAmharic = "ቅድስት አርሴማ ሰማዕትና የማይናወጥ ጽናትዋ",
      titleEnglish = "The Holy Virgin Martyr St. Arsema and Her Unshakable Crown",
      verseReference = "ራእይ 2፥10 • Revelation 2:10",
      verseTextAmharic = "«እስከ ሞት ድረስ የታመንህ ሁን የሕይወትንም አክሊል እሰጥሃለሁ።»",
      verseTextEnglish = "«Be faithful until death, and I will give you the crown of life.»",
      storyAmharic = """
        ቅድስት አርሴማ በሮም ዘመነ ሰማዕታት ያደገች ንጽሕት ድንግልና የክርስቶስ ሙሽራ ነበረች። ንጉሡ ዲዮቅልጥያኖስ በውበቷ ተማርኮ ሊያገባት በፈለገ ጊዜ፣ የዚህን ዓለም ከንቱ ክብር ንቃ ከእናቷ ቅድስት አጋታና ከመነኮሳት እህቶቿ ጋር ወደ አርሜንያ ተሰደደች።

        ንጉሥ ድርጣድስም ሊያስገድዳት በሞከረ ጊዜ በጾምና በጸሎት ብርታት አሸነፈችው። እጅግ አሰቃቂ መከራዎችን በታላቅ የክርስቶስ ፍቅር ታግሣ ሰማዕትነትን ተቀበለች። ዛሬም በደብረ ኃይል ሰንበት ት/ቤታችን ወጣቶች ሁሉ የእሷን ንጽሕና፣ ድፍረትና የእምነት ጽናት አርዓያ አድርገው ይጓዛሉ።
      """.trimIndent(),
      storyEnglish = """
        St. Arsema was a consecrated virgin of noble beauty during the Roman persecutions. When Emperor Diocletian sought her for his bride, she renounced all worldly pomp and fled alongside her abbess St. Agatha and companions to Armenia.

        When King Tiridates attempted to overcome her chastity, the holy virgin defeated his royal guards through divine strength born of fasting and prayer. Enduring torment without yielding, she was crowned as a luminous martyr of Christ. Her purity, boldness, and perseverance inspire all members of Meserete Hiwot Sunday School.
      """.trimIndent(),
      moralAmharic = "ለክርስቶስ ታማኝ መሆን ከዓለም ንጉሦች ክብር ሁሉ ይበልጣል። ንጽሕናንና እውነትን በመያዝ ለወጣቶች የጽናት ምልክት ናት።",
      moralEnglish = "Purity and steadfast dedication to Christ transcend all worldly glory and temporary praise. St. Arsema stands as a beacon of youth holiness.",
      prayerAmharic = "ቅድስት አርሴማ ሆይ! በእምነትሽ ጽናት፣ በንጽሕናሽም በረከት የሰንበት ት/ቤታችንን ልጆች ከኃጢአት ማዕበል ጠብቂልን፤ የሕይወት አክሊልንም እንድንወርስ ጸልይልን። አሜን።",
      prayerEnglish = "O Holy Martyr St. Arsema, bride of Christ, pray for our Sunday School members that we may maintain purity of soul and body, remaining faithful unto eternity. Amen."
    ),
    BibleStory(
      id = "story_3",
      dayNumber = 3,
      titleAmharic = "ንጉሥ ዳዊትና ታቦተ ጽዮን በዝማሬ ወደ ኢየሩሳሌም መግባቷ",
      titleEnglish = "King David Dancing with Joy Before the Ark of the Covenant",
      verseReference = "2 ሳሙኤል 6፥14 • 2 Samuel 6:14",
      verseTextAmharic = "«ዳዊትም በእግዚአብሔር ፊት በሙሉ ኃይሉ ይዘፍን (ያመሰግን) ነበር፤ ዳዊትም የበፍታ ኤፉድ ለብሶ ነበር።»",
      verseTextEnglish = "«Then David danced before the Lord with all his might; and David was wearing a linen ephod.»",
      storyAmharic = """
        ንጉሥ ቅዱስ ዳዊት የእግዚአብሔር የቃል ኪዳኑ ታቦት ወደ ኢየሩሳሌም በክብር በገባች ጊዜ፣ የንጉሥነቱን ልብስ አውልቆ እንደ ተራ አገልጋይ በእግዚአብሔር ፊት በሙሉ ኃይሉ፣ በከበሮና በጸናጽል፣ በመሰንቆና በበገና አመሰገነ።

        በቤተክርስቲያናችን የመዝሙርና ሥነ ጥበብ ክፍል (Song & Arts Department) አገልጋዮች ዛሬም በከበሮና በጸናጽል፣ በቃጭልና በዘንግ እግዚአብሔርን በቅኔና በማኅሌት የሚያመሰግኑት የዳዊትን የንጹሕ ልብ ምስጋና በመከተል ነው።
      """.trimIndent(),
      storyEnglish = """
        When the Ark of the God of Israel was brought up into the City of Jerusalem, King David set aside royal airs, donned a priestly linen garment, and praised the Lord with all his heart—accompanied by cymbals, harps, lyres, and joyous shouts.

        To this day in the Ethiopian Orthodox Tewahedo Church, our Song & Arts Department worships God with Kebero drums, Tsenatsil sistrums, and Maqimiya prayer staffs just as King David did, lifting up pure hymns of thanksgiving.
      """.trimIndent(),
      moralAmharic = "እግዚአብሔርን ስናመሰግን በትሕትናና በሙሉ ልብ መሆን አለበት። ምስጋና የሰንበት ት/ቤት አገልጋዮች የሕይወት እስትንፋስ ነው።",
      moralEnglish = "True worship requires genuine humility and wholehearted devotion. Praising God with sacred instruments unites heaven and earth in joy.",
      prayerAmharic = "አቤቱ አምላካችን! እንደ ቅዱስ ዳዊት በንጹሕ ልብና በሙሉ ኃይላችን እንድናመሰግንህ አንደበታችንን በዝማሬ ባርክልን፤ ዝማሬያችንንም በመንግሥተ ሰማያት ተቀበልልን። አሜን።",
      prayerEnglish = "O Lord, sanctify our lips and instruments like King David, that our hymns and Sunday school chants may be a sweet-smelling offering before Your throne. Amen."
    ),
    BibleStory(
      id = "story_4",
      dayNumber = 4,
      titleAmharic = "ቸሩ ሳምራዊና የወንድማማችነት ፍቅር",
      titleEnglish = "The Parable of the Good Samaritan: Compassion in Action",
      verseReference = "ሉቃስ 10፥37 • Luke 10:37",
      verseTextAmharic = "«ኢየሱስም፦ ሂድ፥ አንተም እንዲሁ አድርግ አለው።»",
      verseTextEnglish = "«Then Jesus said to him, 'Go and do likewise.'»",
      storyAmharic = """
        አንድ ሰው ከኢየሩሳሌም ወደ ኢያሪኮ ሲወርድ በወንበዴዎች እጅ ወደቀ፤ ደብድበውም በሞትና በሕይወት መካከል ጥለውት ሄዱ። ካህኑና ሌዋዊው ባዩት ጊዜ መንገድ ቀይረው አለፉ። ነገር ግን አንድ ሳምራዊ በመንገድ ሲሄድ አየውና አዘነለት፤ ቁስሉንም በዘይትና በወይን ጠጅ አክሞ በበቅሎው ላይ አስቀምጦ ወደ እንግዳ ማረፊያ ወሰደው፤ ገንዘብም ከፍሎ አስጠነቀቀው።

        ይህ ታሪክ የክርስቶስን ቤዛነትና እኛም ለተቸገሩ ወገኖቻችን በበጎ አድራጎት እጃችንን እንድንዘረጋ ያስተምረናል።
      """.trimIndent(),
      storyEnglish = """
        A certain man fell among thieves while traveling from Jerusalem down to Jericho; they stripped and beat him, leaving him half dead. Both a priest and a Levite passed by on the other side. But a Samaritan, who was despised by the Jews, had compassion on him, bound his wounds with oil and wine, brought him to an inn, and paid for his complete recovery.

        Christ teaches us that true love of neighbor breaks all barriers of prejudice. In our Sunday school community, Christian charity and mutual care are our foremost duties.
      """.trimIndent(),
      moralAmharic = "የክርስቲያን እውነተኛ ማንነት የሚለካው በተግባር በሚገለጥ ፍቅር ነው። የተቸገሩትን መርዳት ለክርስቶስ ማድረግ ነው።",
      moralEnglish = "Christian faith is proved through active mercy. Touching the wounds of the needy is ministering to Christ Himself.",
      prayerAmharic = "አምላካችን ኢየሱስ ክርስቶስ ሆይ! እንደ ቸሩ ሳምራዊ ሩኅሩኅ ልብን ስጠን፤ ወገኖቻችንን ያለ አድልዎ እንድንወድድና እንድንረዳ አቅም ሁነን። አሜን።",
      prayerEnglish = "Lord Jesus Christ, give us tender hearts like the Good Samaritan, that we may bring comfort, healing, and brotherly love to all around us. Amen."
    ),
    BibleStory(
      id = "story_5",
      dayNumber = 5,
      titleAmharic = "ቃና ዘገሊላና የእመቤታችን የቅድስት ድንግል ማርያም አማላጅነት",
      titleEnglish = "The Wedding at Cana and the Holy Theotokos' Intercession",
      verseReference = "ዮሐንስ 2፥5 • John 2:5",
      verseTextAmharic = "«እናቱም ለአገልጋዮቹ፦ የሚላችሁን ሁሉ አድርጉ አለቻቸው።»",
      verseTextEnglish = "«His mother said to the servants, 'Whatever He says to you, do it.'»",
      storyAmharic = """
        በቃና ዘገሊላ ሰርግ ተደርጎ ነበር፤ የሠርገኞቹም የወይን ጠጅ ባለቀ ጊዜ ቤተሰቡ በጭንቀት ወደቁ። እመቤታችን ቅድስት ድንግል ማርያም የሰዎቹን ሐዘን ተመልክታ ወደ ልጇ ቀርባ «የወይን ጠጅ እኮ የላቸውም» ብላ ለመነችው። 

        አገልጋዮቹንም «የሚላችሁን ሁሉ አድርጉ» አለቻቸው። ጌታችንም ውኃውን ወደ መልካም የወይን ጠጅ ለወጠው። ይህ የእመቤታችንን የቅርብ እናታዊ ርኅራኄና አማላጅነት ምስክር ነው።
      """.trimIndent(),
      storyEnglish = """
        At a wedding in Cana of Galilee, the wine ran short, threatening great embarrassment to the newlyweds. The Holy Virgin Mary noticed their distress and approached her Son with maternal compassion: 'They have no wine.'

        Turning to the servants, she gave the timeless golden counsel: 'Whatever He says to you, do it.' Our Lord transformed the six stone jars of water into the finest wine, manifesting His glory. Her intercession remains our sweetest solace in every time of need.
      """.trimIndent(),
      moralAmharic = "እመቤታችን ቅድስት ድንግል ማርያም ችግራችንን የምታውቅና የምታስወግድ የሕይወት እናት ናት። እርሷ እንዳዘዘችን የክርስቶስን ቃል እንስማ።",
      moralEnglish = "The Blessed Virgin Mary understands our unspoken sorrows and intercedes with motherly solicitude. Following Christ's command turns sorrow into blessing.",
      prayerAmharic = "ድንግል ማርያም ሆይ! በቃና ዘገሊላ የሠርገኞቹን ሐዘን ወደ ደስታ እንደለወጥሽ፣ በእኛም ሕይወትና በሰንበት ት/ቤታችን ጉድለታችንን ሁሉ በጸሎትሽ ሙይልን። አሜን።",
      prayerEnglish = "O Most Holy Theotokos Virgin Mary, as you turned shame into joy at Cana of Galilee, intercede for our families and Sunday School, filling our spiritual vessels with grace. Amen."
    )
  )
}
