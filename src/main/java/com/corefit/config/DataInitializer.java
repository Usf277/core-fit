package com.corefit.config;

import com.corefit.entity.City;
import com.corefit.entity.Governorate;
import com.corefit.repository.CityRepo;
import com.corefit.repository.GovernorateRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class DataInitializer implements CommandLineRunner {
    @Autowired
    private GovernorateRepo governorateRepository;
    @Autowired
    private CityRepo cityRepository;

    @Override
    public void run(String... args) {
        if (governorateRepository.count() == 0) {
            Map<String, String[]> governorateCities = new HashMap<>();

            // Cairo Governorate
            governorateCities.put("Cairo", new String[]{
                    "Cairo", "New Cairo", "15th of May City",
                    "El Maadi", "Nasr City", "Zamalek",
                    "Sayeda Zeinab", "Heliopolis", "Shubra",
                    "Matariya", "Ain Shams", "El Salam",
                    "Helwan", "El Basateen", "El Daher",
                    "Abbassia", "Mokattam", "Garden City"
            });

            // Alexandria Governorate
            governorateCities.put("Alexandria", new String[]{
                    "Alexandria", "Borg El Arab", "New Borg El Arab", "Abu Qir",
                    "El Amreya", "El Montazah", "El Gomrok", "El Agami", "El Mandara",
                    "Sidi Gaber", "Smouha", "Ibrahimia", "Chatby", "Anfoushi",
                    "El Mansheya", "El Raml", "El Wardian",
                    "Kafr Abdo", "Glim", "Bahary"
            });

            // Giza Governorate
            governorateCities.put("Giza", new String[]{
                    "Giza", "6th of October City", "Sheikh Zayed City", "El Haram",
                    "El Dokki", "El Mohandessin", "El Agouza", "El Badrasheen",
                    "El Hawamdeya", "El Ayyat", "El Wahat El Bahariya",
                    "El Saff", "El Atfih", "El Kerdasa", "El Warraq",
                    "Imbaba", "Boulaq El Dakrour", "Omrania"
            });

            // Sharkia Governorate
            governorateCities.put("Sharkia", new String[]{
                    "Zagazig", "10th of Ramadan City", "Belbeis", "Abu Hammad",
                    "El Husseiniya", "Minya El Qamh", "Faqous", "El Qurein",
                    "El Ibrahimiya", "El Salhiya El Gedida", "El Qanayat", "Kafr Saqr",
                    "Awlad Saqr", "Hihya", "Mashtoul El Souq", "Diyarb Negm",
                    "Abu Kebir", "El Asher Men Ramadan"
            });

            // Dakahlia Governorate
            governorateCities.put("Dakahlia", new String[]{
                    "Mansoura", "Talkha", "Mit Ghamr", "Aga",
                    "El Senbellawein", "El Matareya", "Sherbin", "El Gamaliya",
                    "El Manzala", "Bilqas", "Mit Salsil", "Dikirnis",
                    "Bani Ebeid", "El Kurdi", "Miniet El Nasr", "Tami El Amdid",
                    "Nabaru", "Ras El Bar"
            });

            // Beheira Governorate
            governorateCities.put("Beheira", new String[]{
                    "Damanhour", "Kafr El Dawwar", "Rashid", "Edku",
                    "Abu El Matamir", "El Delengat", "El Mahmoudiya", "El Rahmaniya",
                    "Hosh Issa", "Shubrakhit", "Itay El Barud", "El Nubariya",
                    "Wadi El Natrun", "Kom Hamada", "Badr",
                    "Abu Hummus", "Idku", "El Haddadi"
            });

            // Qalyubia Governorate
            governorateCities.put("Qalyubia", new String[]{
                    "Banha", "Qalyub", "Shubra El Kheima", "El Khanka",
                    "El Qanater El Khayreya", "Tukh", "El Obour", "Kafr Shukr",
                    "Shibin El Qanater", "Al Khosous", "Qaha", "Abu Zaabal",
                    "Mostorod", "Shoubra Elkhima", "El Qanatir El Khairiya",
                    "Bahtim", "Orabi", "Kafr El Gazar"
            });

            // Monufia Governorate
            governorateCities.put("Monufia", new String[]{
                    "Shibin El Kom", "Menouf", "Sadat City", "El Bagour",
                    "El Shohada", "Ashmoun", "Quesna", "Berket El Sab",
                    "Tala", "Sers El Lyan", "Shanawan", "Mit Bara",
                    "El Kawady", "Abu El Nomros", "Singar",
                    "El Khatatba", "Mit Masoud", "El Salamlik"
            });

            // Gharbia Governorate
            governorateCities.put("Gharbia", new String[]{
                    "Tanta", "El Mahalla El Kubra", "Kafr El Zayat", "Zefta",
                    "El Santa", "Basyoun", "Qutur", "Samannoud",
                    "Kotoor", "El Hayatem", "Nadima", "Mahallet Marhoum",
                    "Hesset Abar", "Mahallet Marhum", "Ebshaway El Malaq",
                    "Samatay", "Nashart", "Mahallet Ziyad"
            });

            // Kafr El Sheikh Governorate
            governorateCities.put("Kafr El Sheikh", new String[]{
                    "Kafr El Sheikh", "Desouk", "El Hamool", "El Reyad",
                    "Bila", "Metoubes", "Fuwwah", "Sidi Salem",
                    "Qaleen", "El Burullus", "Baltim", "Biyala",
                    "Menyat El Morshed", "El Boghdadi", "El Bordees",
                    "El Manzala", "Minyat El Ashraf", "El Shahid"
            });

            // Faiyum Governorate
            governorateCities.put("Faiyum", new String[]{
                    "Faiyum", "New Faiyum", "Tamiya", "Sinnuris",
                    "Ibsheway", "Yousef El Seddik", "Etsa", "El Agamiyin",
                    "El Nazla", "Qasr El Bassel", "El Gharak", "Tersa",
                    "Sennoris", "El Rawda", "Manshaat Abdallah",
                    "Ezbet El Khayyat", "El Hadka", "El Mansheya"
            });

            // Minya Governorate
            governorateCities.put("Minya", new String[]{
                    "Minya", "New Minya", "El Idwa", "Maghagha",
                    "Bani Mazar", "Matay", "Samalut", "Abu Qurqas",
                    "Mallawi", "Deir Mawas", "El Edwa", "El Minya El Gadida",
                    "Nazlet Hussein Ali", "Bardanoha", "Damaris",
                    "Taha Hussein", "Bani Ahmed", "Tehna El Gabal"
            });

            // Asyut Governorate
            governorateCities.put("Asyut", new String[]{
                    "Asyut", "New Asyut", "El Badari", "El Qusiya",
                    "El Fath", "El Ghanayem", "Sahel Selim", "Abnoub",
                    "Abu Tig", "Dairut", "Manfalut", "Sodfa",
                    "El Balyana", "Nazlet Abdellah", "Beni Ibrahim",
                    "El Wasti", "El Zarabi", "Arab El Awamer"
            });

            // Sohag Governorate
            governorateCities.put("Sohag", new String[]{
                    "Sohag", "New Sohag", "El Balyana", "El Maragha",
                    "El Monshah", "Girga", "Dar El Salam", "Akhmim",
                    "Sakulta", "Tahta", "Tema", "Juhayna El Gharbiyah",
                    "El Kawthar", "El Usayrat", "El Mazalu",
                    "Beit Dawood", "El Maha", "Awlad Elias"
            });

            // Qena Governorate
            governorateCities.put("Qena", new String[]{
                    "Qena", "New Qena", "El Waqf", "Qus",
                    "Dishna", "Naga Hammadi", "Abu Tesht", "Farshut",
                    "Qift", "Naqada", "El Bayada", "El Qarn",
                    "El Saniya", "El Marashda", "Abnud",
                    "El Higaziya", "Dandara", "El Ghorayib"
            });

            // Aswan Governorate
            governorateCities.put("Aswan", new String[]{
                    "Aswan", "New Aswan", "Abu Simbel", "Kalabsha",
                    "Sebaiya", "Kom Ombo", "Edfu", "Daraw",
                    "Nasr El Nuba", "Aswan El Gadida", "Nasr",
                    "El Basaliya", "El Sad El Ali", "Khor El Saha",
                    "El Mahrousa", "El Malek El Nasser", "El Burj", "Gharb Soheil"
            });

            // Luxor Governorate
            governorateCities.put("Luxor", new String[]{
                    "Luxor", "New Luxor", "El Karnak", "El Qurna",
                    "El Bayadiya", "Armant", "Esna", "El Tod",
                    "El Zeiniya", "El Toud", "El Manshaa", "El Boghdadi",
                    "El Madamoud", "Arabet Abydos", "Ezbet El Walda",
                    "El Qornah", "El Debabiya", "El Awamieh"
            });

            // Red Sea Governorate
            governorateCities.put("Red Sea", new String[]{
                    "Hurghada", "El Gouna", "Safaga", "Marsa Alam",
                    "El Quseir", "Shalateen", "Ras Gharib", "Berenice",
                    "Halayeb", "El Shalatin", "Ras Ghareb", "Sahl Hasheesh",
                    "Makadi Bay", "Port Ghalib", "Zafarana",
                    "Wadi El Gemal", "El Hamraween", "Abu Dabbab"
            });

            // New Valley Governorate
            governorateCities.put("New Valley", new String[]{
                    "El Kharga", "El Dakhla", "El Farafra",
                    "El Mut", "Paris", "Balat", "El Sheikh Wali",
                    "Mout", "Baris", "El Gedida", "Bashendi", "Teneida",
                    "El Qasr", "El Gedid", "El Sherka",
                    "El Qalamun", "El Hindaw", "El Zayat"
            });

            // Matrouh Governorate
            governorateCities.put("Matrouh", new String[]{
                    "Marsa Matrouh", "El Alamein", "El Dabaa", "El Sallum",
                    "Siwa", "El Negaila", "Sidi Barrani", "El Hamam",
                    "El Salloum", "Siwa Oasis", "El Hammam", "Barrani",
                    "El Negila", "El Ubayyid", "El Bagdadi", "El Jarjurab",
                    "El Qasr", "Abu Lahv"
            });

            // North Sinai Governorate
            governorateCities.put("North Sinai", new String[]{
                    "El Arish", "El Sheikh Zuweid", "Rafah", "Bir El Abd",
                    "El Hasana", "Nakhl", "Romani", "Nekhel",
                    "Beer El Abd", "Sheikh Zuwaid", "El Massaeed", "El Rawda",
                    "El Gora", "Baloza", "El Mazr", "El Reesa",
                    "Abu Sweira", "El Tuma"
            });

            // South Sinai Governorate
            governorateCities.put("South Sinai", new String[]{
                    "Sharm El Sheikh", "Dahab", "Nuweiba", "El Tor",
                    "Saint Catherine", "Taba", "Ras Sidr", "Abu Rudeis",
                    "Abu Zenima", "Ras Sudr", "El Nuseirat", "El Wadi",
                    "Abu Sweira", "Wadi Feiran", "El Firan", "Farsh El Azraq",
                    "Abu Rdees", "El Ruwaise"
            });

            // Port Said Governorate
            governorateCities.put("Port Said", new String[]{
                    "Port Said", "Port Fouad", "El Arab", "El Dawahi",
                    "El Ganoub", "El Sharq", "El Manakh", "El Zohour",
                    "El Dawa'i", "El Zuhur", "El Manakh", "El Gharb",
                    "El Dawahy", "El Manasra", "El Qabouty",
                    "El Asafra", "El Kuwait", "El Salam"
            });

            // Suez Governorate
            governorateCities.put("Suez", new String[]{
                    "Suez", "El Arbaeen", "El Ganayen", "El Attaka",
                    "Faisal", "Port Tawfik", "El Salam", "Ain El Sukhna",
                    "El Arba'in", "El Ganayen", "El Tawfiqeya", "El Amal",
                    "El Mustathmarin", "El Adabiya", "El Sokhna",
                    "El Suez El Gadida", "El Manzala", "El Galaa"
            });

            // Ismailia Governorate
            governorateCities.put("Ismailia", new String[]{
                    "Ismailia", "New Ismailia", "El Qantara", "El Qantara East",
                    "El Qantara West", "Tal El Kabir", "Fayed", "El Qassassin",
                    "Abu Suwair", "El Qantara Sharq", "El Qantara Gharb",
                    "El Ismailia El Gadida", "Fayid", "Abu Suweir", "El Qassasin",
                    "El Mahsama", "El Sabaa Abar", "El Manaif"
            });

            // Damietta Governorate
            governorateCities.put("Damietta", new String[]{
                    "Damietta", "New Damietta", "El Zarqa", "Kafr Saad",
                    "Faraskour", "Kafr El Batikh", "Ezbet El Borg", "Ras El Bar",
                    "Fareskour", "El Damietta El Gadida", "El Basarta", "El Roda",
                    "El Senaniya", "Meet Abou Ghaleb", "Shark El Salam",
                    "El Anania", "El Kafr El Qadim", "El Rekabiya"
            });

            // Beni Suef Governorate
            governorateCities.put("Beni Suef", new String[]{
                    "Beni Suef", "New Beni Suef", "El Fashn", "El Wasta",
                    "Beba", "Ihnasiya", "Sumusta", "Nasser",
                    "Biba", "El Beni Suef El Gadida", "Ahnasia",
                    "Sumusta El Waqf", "El Shadmon", "Maymuna",
                    "El Halabiya", "El Kom El Ahmar", "El Halalfa", "El Shantour"
            });

            // Save governorates and their cities
            for (Map.Entry<String, String[]> entry : governorateCities.entrySet()) {
                Governorate governorate = new Governorate();
                governorate.setName(entry.getKey());
                governorate = governorateRepository.save(governorate);

                for (String cityName : entry.getValue()) {
                    if (!cityRepository.existsByNameAndGovernorateId(cityName, governorate.getId())) {
                        City city = new City();
                        city.setName(cityName);
                        city.setGovernorate(governorate);
                        cityRepository.save(city);
                    }
                }
            }
        }
    }
}