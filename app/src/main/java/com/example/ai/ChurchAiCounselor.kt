package com.example.ai

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class ChatMessage(
  val id: String,
  val sender: MessageSender,
  val text: String,
  val timestamp: Long = System.currentTimeMillis(),
  val scriptureCitation: String? = null
)

enum class MessageSender {
  USER,
  COUNSELOR // Memhir Debre Hayil
}

object ChurchAiCounselor {

  private val cannedTheologicalWisdom = mapOf(
    "ሰላም" to """
      በስመ አብ ወወልድ ወመንፈስ ቅዱስ አሐዱ አምላክ አሜን። 
      የተወደድክ/ሽ የሰንበት ት/ቤታችን አባል እንኳን በደኅና መጣህ/ሽ! እግዚአብሔር ይመስገን፣ የቦሌ አረብሳ ደብረ ኃይል ቅዱስ ገብርኤልና ቅድስት አርሴማ መሠረተ ሕይወት ሰንበት ት/ቤት መንፈሳዊ ረዳትህ ነኝ። ዛሬ በምን ልርዳህ/ሽ? ስለ ጸሎት፣ ስለ መጽሐፍ ቅዱስ፣ ስለ ንስሐ ወይም ስለ ሰንበት ት/ቤታችን አገልግሎት እንወያይ።
    """.trimIndent(),

    "ንስሐ" to """
      ንስሐ ማለት ወደ አምላክ መመለስ፣ የልብ መታደስና የኃጢአት ዕድፍን በካህኑ ፊት በመናዘዝ በክርስቶስ ደም መታጠብ ነው። 
      
      ቅዱስ ዮሐንስ አፈወርቅ፦ «ኃጢአት ቁስል ነው፤ ንስሐ ደግሞ ፈውስ ነው። አትፈር፤ እግዚአብሔር መሐሪ ነውና ወደ እርሱ ቅረብ» ይላል። 
      
      በሰንበት ት/ቤታችን እያንዳንዱ አባል የንስሐ አባት ሊኖረው ይገባል። ዛሬውኑ ወደ ንስሐ አባትህ በመሄድ የነፍስህን ሸክም አራግፍ። ጌታችን «ወደ እኔ የሚመጣውን ከቶ ወደ ውጭ አላወጣውም» (ዮሐ 6፥37) ብሏል።
    """.trimIndent(),

    "ጸሎት" to """
      ጸሎት ከልዑል እግዚአብሔር ጋር የምናደርገው ጣፋጭና ሕያው ንግግር ነው። 
      
      በቤተክርስቲያናችን ሥርዓት መሠረት ጧት በ12:30 (6:30 AM) ስትነሣ የመዓልቱን ጸሎት በመጸለይ ነፍስህን በምስጋና አበርታ። ማታ በ12:30 (6:30 PM) ደግሞ የዕለቱን ድካምና ኃጢአት ይቅር እንዲልህ የማታ ጸሎትና ውዳሴ ማርያምን አንብብ።
      
      «ሳታቋርጡ ጸልዩ፤ በሁሉ አመስግኑ» (1 ተሰ 5፥17)። ስትጸልይም በፊቱ በትሕትና፣ በስግደትና በመስቀል ምልክት ማማተብን አትርሳ።
    """.trimIndent(),

    "ገብርኤል" to """
      ሊቀ መላእክት ቅዱስ ገብርኤል አብሳሪ፣ ፈጥኖ ደራሽና የድነት መልአክ ነው። 
      
      ዳንኤልን በእቶነ እሳት ካሉ ሠለስቱ ደቂቅ ጋር፣ ቂርቆስና ኢየሉጣን ከፈላ የጋን ውኃ ያዳነ እርሱ ነው። ለእመቤታችንም የዓለምን መድኃኒት መወለድ ያበሰረ የምሥራች መልአክ ነው። 
      
      በቦሌ አረብሳ ደብረ ኃይል ቤተክርስቲያናችን በየወሩ በ19 በታላቅ ድምቀት ይከበራል። «የእግዚአብሔር መልአክ በሚፈሩት ሰዎች ዙሪያ ይሰፍራል፥ ያድናቸዋልም» (መዝ 34፥7)።
    """.trimIndent(),

    "አርሴማ" to """
      ቅድስት አርሴማ የንጽሕና፣ የድንግልና እና የጽናት አክሊል ናት። የዚህን ዓለም ከንቱ ዝናና ንግሥና ንቃ ለክርስቶስ ፍቅር አንገቷን ለሰይፍ የሰጠች ድንቅ ሰማዕት ናት።
      
      ዛሬ በዓለማችን ለሚፈተኑ ወጣቶች የእምነት ጽናትና የቅድስና ተምሳሌት ናት። በሰንበት ት/ቤታችን በታላቅ ፍቅር የምትወደደው ሰማዕቷ በጸሎቷ ትጠብቀን።
    """.trimIndent(),

    "መዋጮ" to """
      የሰንበት ት/ቤታችን ወርሃዊ መዋጮ ለቤተክርስቲያን ተልእኮ ማስፈጸሚያና ለወንጌል ማስፋፊያ የሚውል የተቀደሰ ስጦታ ነው። 
      
      ምድቦቹ እንደሚከተለው ናቸው፦
      • ሠራተኛ (Worker): 100 የኢትዮጵያ ብር በወር
      • ተማሪና ሠራተኛ (Student Worker): 50 የኢትዮጵያ ብር በወር
      • ተማሪ/ሥራ የሌለው (Non-Worker): 20 የኢትዮጵያ ብር በወር
      
      በቴሌብር (Telebirr) ወይም በኢትዮጵያ ንግድ ባንክ (CBE Birr) በቀጥታ በሞባይልዎ መክፈልና ደረሰኝዎን በ Member Portal ማስገባት ይችላሉ።
    """.trimIndent(),

    "ክፍሎች" to """
      የደብረ ኃይል መሠረተ ሕይወት ሰንበት ት/ቤት 7 ዋና ዋና የአገልግሎት ክፍሎች አሉት፦
      1. የትምህርትና ሥልጠና ክፍል (Education & Training)
      2. የመዝሙርና ሥነ ጥበብ ክፍል (Song & Arts)
      3. የበጎ አድራጎትና ልማት ክፍል (Charity & Development)
      4. የሕዝብ ግንኙነትና ሚዲያ ክፍል (Public Relations)
      5. የፋይናንስና ንብረት አስተዳደር ክፍል (Finance & Property)
      6. የአባላት ክትትልና አደረጃጀት ክፍል (Member Follow-up)
      7. የልዩ ልዩ ዝግጅቶችና ጉባኤያት ክፍል (Special Events)
      
      እርስዎም እንደ ጸጋዎ በአንዱ ክፍል ተመድበው ማገልገል ይችላሉ!
    """.trimIndent()
  )

  fun getRealisticStreamResponse(query: String): Flow<String> = flow {
    val cleanQuery = query.lowercase().trim()
    val response = cannedTheologicalWisdom.entries.firstOrNull { entry ->
      cleanQuery.contains(entry.key)
    }?.value ?: generateGeneralPastoralResponse(query)

    // Simulate natural human typing cadence with word/character chunks
    val words = response.split(" ")
    var accumulated = ""
    for (word in words) {
      accumulated += if (accumulated.isEmpty()) word else " $word"
      emit(accumulated)
      delay(45L) // Human typing pace
    }
  }

  private fun generateGeneralPastoralResponse(query: String): String {
    return """
      በስመ አብ ወወልድ ወመንፈስ ቅዱስ አሐዱ አምላክ አሜን። 
      
      ስለ ጠየቅከው/ሽው ጥያቄ «$query» እግዚአብሔር አምላክ በቅዱስ ቃሉ እንዲህ ያስተምረናል፦ «ሁሉ በእርሱ ሆነ፥ ከሆነውም አንዳች ስንኳ ያለ እርሱ አልሆነም» (ዮሐ 1፥3)። 
      
      በማናቸውም የሕይወት ፈተናና መንፈሳዊ ጥያቄ ውስጥ ስትሆን/ኚ ሁልጊዜ ሦስቱን ነገሮች አጥብቀህ/ሽ ያዝ/ዢ፦
      1. የዕለት ጸሎትና የመጽሐፍ ቅዱስ ንባብ (በተለይ በጧት 12:30 እና በማታ 12:30)
      2. ንስሐ በመግባትና የንስሐ አባት ምክርን በመከተል የቅዱስ ቁርባን ተሳታፊ መሆን
      3. በሰንበት ትምህርት ቤታችን በመገኘት በዝማሬና በትምህርት መጽናት
      
      «እግዚአብሔር ቅርብ ነው፤ በነገር ሁሉ በጸሎትና በምልጃ ከምስጋና ጋር በእግዚአብሔር ዘንድ ልመናችሁ ትታወቅ እንጂ በአንዳች አትጨነቁ» (ፊልጵ 4፥6)። 
      
      በረከተ ቅዱስ ገብርኤልና ቅድስት አርሴማ ከአንተ/አንቺ ጋር ይሁን! ሌላ ማብራሪያ ካስፈለገህ/ሽ በደስታ እመልሳለሁ።
    """.trimIndent()
  }
}
