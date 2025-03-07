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
                    "Cairo", "New Cairo", "6th of October City", "Sheikh Zayed City",
                    "Badr City", "New Administrative Capital", "15th of May City",
                    "El Maadi", "El Nasr City", "El Zamalek", "El Dokki", "El Mohandessin",
                    "El Agouza", "El Haram", "El Giza", "El Sayeda Zeinab"
            });

            // Alexandria Governorate
            governorateCities.put("Alexandria", new String[]{
                    "Alexandria", "Borg El Arab", "New Borg El Arab", "Abu Qir",
                    "El Amreya", "El Montazah", "El Gomrok", "El Agami", "El Mandara",
                    "El Sidi Gaber", "El Smouha", "El Ibrahimia", "El Chatby", "El Anfoushi",
                    "El Mansheya", "El Raml", "El Wardian"
            });

            // Giza Governorate
            governorateCities.put("Giza", new String[]{
                    "Giza", "6th of October", "Sheikh Zayed", "El Haram",
                    "El Dokki", "El Mohandessin", "El Agouza", "El Badrasheen",
                    "El Hawamdeya", "El Ayyat", "El Saf", "El Wahat El Bahariya",
                    "El Saff", "El Atfih", "El Kerdasa", "El Warraq"
            });

            // Sharkia Governorate
            governorateCities.put("Sharkia", new String[]{
                    "Zagazig", "10th of Ramadan City", "Belbeis", "Abu Hammad",
                    "El Husseiniya", "Minya El Qamh", "Faqous", "El Qurein",
                    "El Ibrahimiya", "El Salhiya", "El Qanayat", "El Kafr El Zayat",
                    "El Zaqaziq", "El Sanafa", "El Qantara", "El Qantara El Sharqiya"
            });

            // Dakahlia Governorate
            governorateCities.put("Dakahlia", new String[]{
                    "Mansoura", "Talkha", "Mit Ghamr", "Aga",
                    "El Senbellawein", "El Matareya", "Sherbin", "El Gamaliya",
                    "El Manzala", "El Kurdi", "El Mahalla El Kubra", "El Mahalla El Saghira",
                    "El Dakhla", "El Qantara", "El Qantara El Gharbiya", "El Qantara El Sharqiya"
            });

            // Beheira Governorate
            governorateCities.put("Beheira", new String[]{
                    "Damanhour", "Kafr El Dawwar", "Rashid", "Edku",
                    "Abu El Matamir", "El Delengat", "El Mahmoudiya", "El Rahmaniya",
                    "El Nubariya", "El Sadat City", "El Wadi El Jadid", "El Wadi El Gharbi",
                    "El Wadi El Sharqi", "El Wadi El Qibli", "El Wadi El Bahari", "El Wadi El Qibli"
            });

            // Qalyubia Governorate
            governorateCities.put("Qalyubia", new String[]{
                    "Banha", "Qalyub", "Shubra El Kheima", "El Khanka",
                    "El Qanater El Khayreya", "El Qalyub", "El Shubra El Kheima",
                    "El Tukh", "El Obour", "El Sadat City", "El Sheikh Zayed",
                    "El Sheikh Zayed City", "El Sheikh Zayed District", "El Sheikh Zayed Area",
                    "El Sheikh Zayed Region", "El Sheikh Zayed Zone"
            });

            // Monufia Governorate
            governorateCities.put("Monufia", new String[]{
                    "Shibin El Kom", "Menouf", "Sadat City", "El Bagour",
                    "El Shohada", "El Sadat", "El Sadat City", "El Sadat District",
                    "El Sadat Area", "El Sadat Region", "El Sadat Zone", "El Sadat Quarter",
                    "El Sadat Section", "El Sadat Part", "El Sadat Division", "El Sadat Subdivision"
            });

            // Gharbia Governorate
            governorateCities.put("Gharbia", new String[]{
                    "Tanta", "El Mahalla El Kubra", "Kafr El Zayat", "Zefta",
                    "El Santa", "El Basyoun", "El Qutur", "El Mahalla El Kubra",
                    "El Mahalla El Saghira", "El Mahalla El Kubra City", "El Mahalla El Kubra District",
                    "El Mahalla El Kubra Area", "El Mahalla El Kubra Region", "El Mahalla El Kubra Zone",
                    "El Mahalla El Kubra Quarter", "El Mahalla El Kubra Section"
            });

            // Kafr El Sheikh Governorate
            governorateCities.put("Kafr El Sheikh", new String[]{
                    "Kafr El Sheikh", "Desouk", "El Hamool", "El Reyad",
                    "Bila", "El Metoubes", "El Hamool", "El Reyad", "Bila",
                    "El Metoubes", "El Hamool", "El Reyad", "Bila", "El Metoubes",
                    "El Hamool", "El Reyad"
            });

            // Faiyum Governorate
            governorateCities.put("Faiyum", new String[]{
                    "Faiyum", "New Faiyum", "Tamiya", "Sinnuris",
                    "Ibsheway", "Yousef El Seddik", "Faiyum City", "New Faiyum City",
                    "Tamiya City", "Sinnuris City", "Ibsheway City", "Yousef El Seddik City",
                    "Faiyum District", "New Faiyum District", "Tamiya District", "Sinnuris District"
            });

            // Minya Governorate
            governorateCities.put("Minya", new String[]{
                    "Minya", "New Minya", "El Idwa", "Maghagha",
                    "Bani Mazar", "Matai", "Minya City", "New Minya City",
                    "El Idwa City", "Maghagha City", "Bani Mazar City", "Matai City",
                    "Minya District", "New Minya District", "El Idwa District", "Maghagha District"
            });

            // Asyut Governorate
            governorateCities.put("Asyut", new String[]{
                    "Asyut", "New Asyut", "El Badari", "El Qusiya",
                    "El Fath", "El Ghanayem", "Asyut City", "New Asyut City",
                    "El Badari City", "El Qusiya City", "El Fath City", "El Ghanayem City",
                    "Asyut District", "New Asyut District", "El Badari District", "El Qusiya District"
            });

            // Sohag Governorate
            governorateCities.put("Sohag", new String[]{
                    "Sohag", "New Sohag", "El Balyana", "El Maragha",
                    "El Monshah", "Girga", "Sohag City", "New Sohag City",
                    "El Balyana City", "El Maragha City", "El Monshah City", "Girga City",
                    "Sohag District", "New Sohag District", "El Balyana District", "El Maragha District"
            });

            // Qena Governorate
            governorateCities.put("Qena", new String[]{
                    "Qena", "New Qena", "El Waqf", "El Uqsur",
                    "El Qus", "El Qus", "Qena City", "New Qena City",
                    "El Waqf City", "El Uqsur City", "El Qus City", "El Qus City",
                    "Qena District", "New Qena District", "El Waqf District", "El Uqsur District"
            });

            // Aswan Governorate
            governorateCities.put("Aswan", new String[]{
                    "Aswan", "New Aswan", "Abu Simbel", "El Kalabsha",
                    "El Sebaiya", "Kom Ombo", "Aswan City", "New Aswan City",
                    "Abu Simbel City", "El Kalabsha City", "El Sebaiya City", "Kom Ombo City",
                    "Aswan District", "New Aswan District", "Abu Simbel District", "El Kalabsha District"
            });

            // Luxor Governorate
            governorateCities.put("Luxor", new String[]{
                    "Luxor", "New Luxor", "El Karnak", "El Qurna",
                    "El Bayadiya", "El Armant", "Luxor City", "New Luxor City",
                    "El Karnak City", "El Qurna City", "El Bayadiya City", "El Armant City",
                    "Luxor District", "New Luxor District", "El Karnak District", "El Qurna District"
            });

            // Red Sea Governorate
            governorateCities.put("Red Sea", new String[]{
                    "Hurghada", "El Gouna", "Safaga", "Marsa Alam",
                    "El Quseir", "Shalateen", "Hurghada City", "El Gouna City",
                    "Safaga City", "Marsa Alam City", "El Quseir City", "Shalateen City",
                    "Hurghada District", "El Gouna District", "Safaga District", "Marsa Alam District"
            });

            // New Valley Governorate
            governorateCities.put("New Valley", new String[]{
                    "El Kharga", "El Dakhla", "El Farafra", "El Bahariya",
                    "El Siwa", "El Mut", "El Kharga City", "El Dakhla City",
                    "El Farafra City", "El Bahariya City", "El Siwa City", "El Mut City",
                    "El Kharga District", "El Dakhla District", "El Farafra District", "El Bahariya District"
            });

            // Matrouh Governorate
            governorateCities.put("Matrouh", new String[]{
                    "Marsa Matrouh", "El Alamein", "El Dabaa", "El Sallum",
                    "El Siwa", "El Negaila", "Marsa Matrouh City", "El Alamein City",
                    "El Dabaa City", "El Sallum City", "El Siwa City", "El Negaila City",
                    "Marsa Matrouh District", "El Alamein District", "El Dabaa District", "El Sallum District"
            });

            // North Sinai Governorate
            governorateCities.put("North Sinai", new String[]{
                    "El Arish", "El Sheikh Zuweid", "El Rafah", "El Bir El Abd",
                    "El Hasana", "El Nakhl", "El Arish City", "El Sheikh Zuweid City",
                    "El Rafah City", "El Bir El Abd City", "El Hasana City", "El Nakhl City",
                    "El Arish District", "El Sheikh Zuweid District", "El Rafah District", "El Bir El Abd District"
            });

            // South Sinai Governorate
            governorateCities.put("South Sinai", new String[]{
                    "Sharm El Sheikh", "Dahab", "Nuweiba", "El Tur",
                    "Saint Catherine", "El Tor", "Sharm El Sheikh City", "Dahab City",
                    "Nuweiba City", "El Tur City", "Saint Catherine City", "El Tor City",
                    "Sharm El Sheikh District", "Dahab District", "Nuweiba District", "El Tur District"
            });

            // Port Said Governorate
            governorateCities.put("Port Said", new String[]{
                    "Port Said", "New Port Said", "El Arab", "El Dawahi",
                    "El Ganoub", "El Sharq", "Port Said City", "New Port Said City",
                    "El Arab City", "El Dawahi City", "El Ganoub City", "El Sharq City",
                    "Port Said District", "New Port Said District", "El Arab District", "El Dawahi District"
            });

            // Suez Governorate
            governorateCities.put("Suez", new String[]{
                    "Suez", "New Suez", "El Arbaeen", "El Ganayen",
                    "El Suez", "El Attaka", "Suez City", "New Suez City",
                    "El Arbaeen City", "El Ganayen City", "El Suez City", "El Attaka City",
                    "Suez District", "New Suez District", "El Arbaeen District", "El Ganayen District"
            });

            // Ismailia Governorate
            governorateCities.put("Ismailia", new String[]{
                    "Ismailia", "New Ismailia", "El Qantara", "El Qantara El Sharqiya",
                    "El Qantara El Gharbiya", "El Tal El Kabir", "Ismailia City", "New Ismailia City",
                    "El Qantara City", "El Qantara El Sharqiya City", "El Qantara El Gharbiya City",
                    "El Tal El Kabir City", "Ismailia District", "New Ismailia District", "El Qantara District",
                    "El Qantara El Sharqiya District"
            });

            // Damietta Governorate
            governorateCities.put("Damietta", new String[]{
                    "Damietta", "New Damietta", "El Zarqa", "El Kafr El Bahari",
                    "El Kafr El Qibli", "El Raswa", "Damietta City", "New Damietta City",
                    "El Zarqa City", "El Kafr El Bahari City", "El Kafr El Qibli City", "El Raswa City",
                    "Damietta District", "New Damietta District", "El Zarqa District", "El Kafr El Bahari District"
            });

            // Beni Suef Governorate
            governorateCities.put("Beni Suef", new String[]{
                    "Beni Suef", "New Beni Suef", "El Fashn", "El Wasta",
                    "Beba", "Nasser", "Beni Suef City", "New Beni Suef City",
                    "El Fashn City", "El Wasta City", "Beba City", "Nasser City",
                    "Beni Suef District", "New Beni Suef District", "El Fashn District", "El Wasta District"
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