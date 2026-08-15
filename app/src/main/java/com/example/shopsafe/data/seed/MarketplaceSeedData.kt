package com.example.shopsafe.data.seed

import com.example.shopsafe.data.models.ItemCategory
import com.example.shopsafe.data.models.ItemCondition
import com.example.shopsafe.data.models.MarketplaceItem
import java.util.UUID

object MarketplaceSeedData {

    fun get250MarketplaceItems(): List<MarketplaceItem> {
        val items = mutableListOf<MarketplaceItem>()
        val currentTime = System.currentTimeMillis()

        data class SeedTemplate(
            val title: String,
            val price: Double,
            val description: String,
            val category: ItemCategory,
            val condition: ItemCondition,
            val pickupLocation: String,
            val lat: Double,
            val lng: Double,
            val imageUrl: String,
            val sellerName: String,
            val sellerRating: Double,
            val sellerPhone: String,
            val isFacebook: Boolean,
            val isNew: Boolean,
            val distance: Double,
            val dimensions: String,
            val weightLbs: Double,
            val vehicleType: String,
            val isHeavy: Boolean
        )

        val templates = listOf(
            // 1-25: Electronics - Computing & Phones
            SeedTemplate(
                "Apple iPhone 15 Pro Max - 256GB Natural Titanium", 950.0,
                "Unlocked, pristine condition with 98% battery health. Comes with original box, braided USB-C cable and AppleCare+ active.",
                ItemCategory.ELECTRONICS, ItemCondition.USED_LIKE_NEW,
                "742 Market St, San Francisco, CA (Apple Union Square)", 37.7880, -122.4075,
                "https://images.unsplash.com/photo-1695048133142-1a20484d2569?w=800&auto=format&fit=crop&q=80",
                "Elena Vance", 4.9, "(555) 345-6789", true, false, 0.8, "6x3x0.3 in", 0.5, "Sedan", false
            ),
            SeedTemplate(
                "MacBook Pro 16\" M3 Max - 36GB RAM / 1TB SSD", 2450.0,
                "Space Black M3 Max. Liquid Retina XDR screen in mint condition. Perfect for 4K video editing and software engineering.",
                ItemCategory.ELECTRONICS, ItemCondition.USED_LIKE_NEW,
                "500 Howard St, San Francisco, CA (Lobby Exchange)", 37.7895, -122.3990,
                "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=800&auto=format&fit=crop&q=80",
                "Marcus Brody", 5.0, "(555) 890-1234", false, false, 1.2, "14x10x0.7 in", 4.7, "Sedan", false
            ),
            SeedTemplate(
                "Sony PlayStation 5 Disc Edition + 2 DualSense Controllers", 420.0,
                "PS5 Disc Console with two wireless controllers, charging dock, and Spider-Man 2 + God of War Ragnarok discs.",
                ItemCategory.ELECTRONICS, ItemCondition.USED_LIKE_NEW,
                "1200 Van Ness Ave, San Francisco, CA", 37.7850, -122.4210,
                "https://images.unsplash.com/photo-1606813907291-d86edd9b94ad?w=800&auto=format&fit=crop&q=80",
                "Jason Kim", 4.8, "(555) 456-7890", true, false, 1.4, "15x10x4 in", 9.9, "Sedan", false
            ),
            SeedTemplate(
                "Dell UltraSharp 32\" 4K UHD USB-C Hub Monitor (U3223QE)", 480.0,
                "IPS Black technology with 2000:1 contrast ratio, 90W power delivery, and built-in KVM switch. Clean screen, no dead pixels.",
                ItemCategory.ELECTRONICS, ItemCondition.USED_LIKE_NEW,
                "850 Valencia St, San Francisco, CA", 37.7590, -122.4215,
                "https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=800&auto=format&fit=crop&q=80",
                "Chloe Bennett", 4.9, "(555) 234-9876", false, false, 2.1, "28x19x9 in", 22.0, "Sedan", false
            ),
            SeedTemplate(
                "Sony WH-1000XM5 Wireless Noise Canceling Headphones", 240.0,
                "Industry-leading noise cancellation, silver finish with carrying case, 3.5mm audio jack, and charging cable. Like new.",
                ItemCategory.ELECTRONICS, ItemCondition.USED_LIKE_NEW,
                "201 3rd St, San Francisco, CA (SFMOMA Plaza)", 37.7858, -122.4011,
                "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&auto=format&fit=crop&q=80",
                "David Ross", 4.7, "(555) 312-7890", true, false, 0.9, "9x8x3 in", 1.2, "Sedan", false
            ),
            SeedTemplate(
                "Canon EOS R6 Mark II Mirrorless Camera + RF 24-105mm Lens", 1850.0,
                "24.2 MP Full-Frame sensor, 40 fps electronic shutter, 4K60p 10-bit video. Shutter count under 3,500. Includes 2 batteries.",
                ItemCategory.ELECTRONICS, ItemCondition.USED_LIKE_NEW,
                "1000 Chestnut St, San Francisco, CA", 37.8020, -122.4210,
                "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=800&auto=format&fit=crop&q=80",
                "Rachel Zhang", 5.0, "(555) 789-0123", false, false, 2.5, "10x8x6 in", 3.4, "Sedan", false
            ),
            SeedTemplate(
                "Apple iPad Pro 12.9\" M2 128GB Wi-Fi + Apple Pencil 2", 720.0,
                "Liquid Retina XDR mini-LED display. Comes with 2nd gen Apple Pencil, Smart Folio magnetic case, and 30W fast charger.",
                ItemCategory.ELECTRONICS, ItemCondition.USED_LIKE_NEW,
                "350 Bay St, San Francisco, CA (Fisherman's Wharf)", 37.8055, -122.4135,
                "https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=800&auto=format&fit=crop&q=80",
                "Kevin Patel", 4.9, "(555) 654-3210", true, false, 2.8, "12x9x0.4 in", 1.8, "Sedan", false
            ),
            SeedTemplate(
                "Meta Quest 3 512GB VR Headset + Elite Strap with Battery", 430.0,
                "Next-gen mixed reality headset with pancake lenses and 4K+ Infinite Display. Includes controllers and silicone facial interface.",
                ItemCategory.ELECTRONICS, ItemCondition.USED_LIKE_NEW,
                "650 Townsend St, San Francisco, CA", 37.7712, -122.4042,
                "https://images.unsplash.com/photo-1622979135225-d2ba269bc1df?w=800&auto=format&fit=crop&q=80",
                "Alex Rivera", 4.8, "(555) 987-6543", false, false, 1.5, "10x8x6 in", 2.2, "Sedan", false
            ),
            SeedTemplate(
                "Bose Smart Soundbar 900 with Dolby Atmos", 450.0,
                "Premium wireless soundbar with upward-firing dipole transducers for immersive spatial audio. HDMI eARC and AirPlay 2 supported.",
                ItemCategory.ELECTRONICS, ItemCondition.USED_GOOD,
                "1500 California St, San Francisco, CA", 37.7910, -122.4190,
                "https://images.unsplash.com/photo-1545454675-3531b543be5d?w=800&auto=format&fit=crop&q=80",
                "Samantha Reed", 4.9, "(555) 876-5432", true, false, 1.7, "42x4x2.5 in", 12.6, "Sedan", false
            ),
            SeedTemplate(
                "Nintendo Switch OLED Model - White Edition + Mario Kart 8", 260.0,
                "Vibrant 7-inch OLED screen, 64GB storage, enhanced audio dock. Screen protector installed since day one. No stick drift.",
                ItemCategory.ELECTRONICS, ItemCondition.USED_LIKE_NEW,
                "400 Castro St, San Francisco, CA", 37.7610, -122.4350,
                "https://images.unsplash.com/photo-1578303512597-81e6cc155b3e?w=800&auto=format&fit=crop&q=80",
                "Tyler Woods", 4.7, "(555) 543-2109", true, false, 2.9, "10x5x2 in", 1.5, "Sedan", false
            ),
            SeedTemplate(
                "DJI Mini 4 Pro Drone Fly More Combo Plus", 790.0,
                "Under 249g ultralight foldable drone with 4K/60fps HDR true vertical shooting, omnidirectional obstacle sensing, 3 batteries.",
                ItemCategory.ELECTRONICS, ItemCondition.USED_LIKE_NEW,
                "100 Marina Blvd, San Francisco, CA (Marina Green Safe Zone)", 37.8040, -122.4380,
                "https://images.unsplash.com/photo-1527977966376-1c8408f9f108?w=800&auto=format&fit=crop&q=80",
                "Liam O'Connor", 5.0, "(555) 432-1098", false, false, 3.2, "11x9x5 in", 2.0, "Sedan", false
            ),
            SeedTemplate(
                "NVIDIA GeForce RTX 4080 Super Founders Edition 16GB", 880.0,
                "High performance GPU with DLSS 3.5, Ada Lovelace architecture, 16GB GDDR6X. Never used for mining, original packaging.",
                ItemCategory.ELECTRONICS, ItemCondition.USED_LIKE_NEW,
                "300 4th St, San Francisco, CA (Metreon Exchange)", 37.7830, -122.4030,
                "https://images.unsplash.com/photo-1587202372775-e229f172b9d7?w=800&auto=format&fit=crop&q=80",
                "Brandon Miller", 4.9, "(555) 321-0987", true, false, 0.7, "13x6x3 in", 4.8, "Sedan", false
            ),
            SeedTemplate(
                "Keychron Q1 Pro Wireless Custom Mechanical Keyboard", 150.0,
                "Full aluminum CNC body, Gateron Jupiter Banana switches, double-gasket design, RGB backlighting with hot-swap PCB.",
                ItemCategory.ELECTRONICS, ItemCondition.USED_LIKE_NEW,
                "550 16th St, San Francisco, CA (Mission Bay)", 37.7670, -122.3920,
                "https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=800&auto=format&fit=crop&q=80",
                "Maya Lin", 4.9, "(555) 210-9876", false, false, 1.9, "14x6x2 in", 4.2, "Sedan", false
            ),
            SeedTemplate(
                "LG C3 55\" OLED 4K Smart TV 120Hz Gaming TV", 750.0,
                "Self-lit OLED pixels, α9 AI Processor Gen6, 4x HDMI 2.1 ports, G-Sync & FreeSync compatible. Includes Magic Remote.",
                ItemCategory.ELECTRONICS, ItemCondition.USED_GOOD,
                "1800 Polk St, San Francisco, CA", 37.7935, -122.4225,
                "https://images.unsplash.com/photo-1593784991095-a205069470b6?w=800&auto=format&fit=crop&q=80",
                "Christopher Nolan", 4.8, "(555) 109-8765", true, false, 1.6, "48x28x4 in", 35.0, "SUV", true
            ),
            SeedTemplate(
                "Shure SM7B Cardioid Dynamic Microphone + Cloudlifter CL-1", 310.0,
                "Legendary broadcast microphone for studio recording and podcasting. Includes Cloudlifter mic activator and XLR cable.",
                ItemCategory.ELECTRONICS, ItemCondition.USED_LIKE_NEW,
                "1250 Folsom St, San Francisco, CA (SOMA Safe Zone)", 37.7750, -122.4110,
                "https://images.unsplash.com/photo-1590602847861-f357a9332bbc?w=800&auto=format&fit=crop&q=80",
                "Jordan Bell", 5.0, "(555) 098-7654", false, false, 1.1, "12x7x5 in", 2.8, "Sedan", false
            ),
            SeedTemplate(
                "Samsung Galaxy S24 Ultra 512GB Titanium Gray", 820.0,
                "Snapdragon 8 Gen 3, 200MP camera, built-in S Pen, flat Dynamic AMOLED 2X display. Factory unlocked for all carriers.",
                ItemCategory.ELECTRONICS, ItemCondition.USED_LIKE_NEW,
                "800 Market St, San Francisco, CA", 37.7865, -122.4060,
                "https://images.unsplash.com/photo-1610945415295-d9bbf067e59c?w=800&auto=format&fit=crop&q=80",
                "Emily Watson", 4.9, "(555) 987-1234", true, false, 0.6, "6.5x3x0.3 in", 0.6, "Sedan", false
            ),
            SeedTemplate(
                "Apple Watch Ultra 2 49mm Titanium GPS + Cellular", 550.0,
                "Rugged aerospace titanium case, sapphire front crystal, Precision dual-frequency GPS. Orange Ocean Band included.",
                ItemCategory.ELECTRONICS, ItemCondition.USED_LIKE_NEW,
                "450 Sutter St, San Francisco, CA", 37.7895, -122.4080,
                "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=800&auto=format&fit=crop&q=80",
                "Nathan Drake", 4.9, "(555) 876-2345", false, false, 0.7, "5x3x2 in", 0.4, "Sedan", false
            ),
            SeedTemplate(
                "Anker SOLIX C1000 Portable Power Station (1056Wh)", 590.0,
                "LiFePO4 battery with 3000+ cycles, 1800W AC output (Surge 2400W), charges 0-100% in 58 mins. Perfect for camping/outages.",
                ItemCategory.ELECTRONICS, ItemCondition.NEW,
                "100 Mission St, San Francisco, CA (ShopSafe Hub)", 37.7920, -122.3950,
                "https://images.unsplash.com/photo-1597872200969-2b65d56bd16b?w=800&auto=format&fit=crop&q=80",
                "ShopSafe Official Storefront", 5.0, "(800) 555-SAFE", false, true, 0.5, "15x9x10 in", 28.4, "Sedan", false
            ),
            SeedTemplate(
                "Kindle Scribe 64GB 10.2\" Paperwhite Display + Premium Pen", 280.0,
                "Read and write naturally on 300 ppi glare-free front-lit screen. Includes Premium Pen, leather folio cover, and replacement nibs.",
                ItemCategory.ELECTRONICS, ItemCondition.USED_LIKE_NEW,
                "1400 Haight St, San Francisco, CA", 37.7695, -122.4470,
                "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=800&auto=format&fit=crop&q=80",
                "Sophia Martinez", 4.8, "(555) 765-3456", true, false, 3.1, "10x8x0.3 in", 1.2, "Sedan", false
            ),
            SeedTemplate(
                "Sonos Move 2 Portable Smart Speaker with Bluetooth & Wi-Fi", 320.0,
                "Stereo sound outdoors and indoors, up to 24-hour battery life, IP56 weather resistance, automatic Trueplay tuning.",
                ItemCategory.ELECTRONICS, ItemCondition.USED_LIKE_NEW,
                "2200 Fillmore St, San Francisco, CA (Pacific Heights)", 37.7890, -122.4340,
                "https://images.unsplash.com/photo-1545454675-3531b543be5d?w=800&auto=format&fit=crop&q=80",
                "Lucas Meyer", 4.9, "(555) 654-4567", false, false, 2.2, "10x6x5 in", 6.6, "Sedan", false
            ),

            // 21-45: Vehicles & Transport
            SeedTemplate(
                "2021 Honda Civic EX Sedan - Low Mileage (24k mi)", 18500.0,
                "Single owner Honda Civic EX with only 24,000 miles. Clean title, regular dealer service, sunroof, Apple CarPlay, lane assist.",
                ItemCategory.VEHICLES, ItemCondition.USED_GOOD,
                "1200 Van Ness Ave, San Francisco, CA", 37.7850, -122.4210,
                "https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?w=800&auto=format&fit=crop&q=80",
                "Marcus Brody", 5.0, "(555) 890-1234", true, false, 1.8, "183x71x56 in", 2900.0, "Sedan", false
            ),
            SeedTemplate(
                "2022 Tesla Model 3 Long Range AWD - 32k Miles", 26900.0,
                "Clean title dual motor Model 3. Acceleration boost enabled, 19\" Sport wheels, premium white interior, full tint, garage kept.",
                ItemCategory.VEHICLES, ItemCondition.USED_LIKE_NEW,
                "2550 Van Ness Ave, San Francisco, CA", 37.7990, -122.4240,
                "https://images.unsplash.com/photo-1560958089-b8a1929cea89?w=800&auto=format&fit=crop&q=80",
                "Travis Scott", 5.0, "(555) 543-8901", false, false, 2.6, "185x73x57 in", 4050.0, "Sedan", false
            ),
            SeedTemplate(
                "Specialized Turbo Vado 4.0 Electric Commuter Bike (Medium)", 2100.0,
                "Class 3 e-bike (28 mph assist) with 710Wh integrated battery, custom display, front suspension, hydraulic disc brakes, rear rack.",
                ItemCategory.VEHICLES, ItemCondition.USED_LIKE_NEW,
                "600 Stanyan St, San Francisco, CA (Golden Gate Park Entrance)", 37.7700, -122.4530,
                "https://images.unsplash.com/photo-1571068316344-75bc76f77890?w=800&auto=format&fit=crop&q=80",
                "Claire Redfield", 4.9, "(555) 432-9012", true, false, 3.4, "72x28x42 in", 52.0, "SUV", true
            ),
            SeedTemplate(
                "Segway Ninebot Max G2 Electric Kick Scooter", 520.0,
                "Dual suspension, 43-mile range, 22 mph top speed, built-in turn signals and Apple Find My support. 380 total miles.",
                ItemCategory.VEHICLES, ItemCondition.USED_LIKE_NEW,
                "500 2nd St, San Francisco, CA (South Park Safe Zone)", 37.7820, -122.3950,
                "https://images.unsplash.com/photo-1558981403-c5f9899a28bc?w=800&auto=format&fit=crop&q=80",
                "Sam Fischer", 4.8, "(555) 321-0123", true, false, 1.0, "48x21x47 in", 53.0, "Sedan", false
            ),
            SeedTemplate(
                "2020 Trek Domane SL 5 Carbon Road Bike (56cm)", 1650.0,
                "500 Series OCLV Carbon frame with front and rear IsoSpeed decouplers for smooth rides over rough roads. Shimano 105 groupset.",
                ItemCategory.VEHICLES, ItemCondition.USED_LIKE_NEW,
                "900 North Point St, San Francisco, CA (Ghirardelli Square)", 37.8060, -122.4230,
                "https://images.unsplash.com/photo-1485965120184-e220f721d03e?w=800&auto=format&fit=crop&q=80",
                "Julian Croft", 5.0, "(555) 210-1234", false, false, 2.7, "68x20x39 in", 21.5, "SUV", false
            ),
            SeedTemplate(
                "Super73-S2 High Performance Electric Motorbike (Hudson Blue)", 1850.0,
                "Iconic moto-style frame, 2000W peak motor, up to 75 miles range in PAS mode. Includes passenger foot pegs and upgraded headlight.",
                ItemCategory.VEHICLES, ItemCondition.USED_LIKE_NEW,
                "1600 Owens St, San Francisco, CA (Mission Bay Exchange)", 37.7690, -122.3930,
                "https://images.unsplash.com/photo-1558981806-ec527fa84c39?w=800&auto=format&fit=crop&q=80",
                "Austin Powers", 4.7, "(555) 109-2345", true, false, 1.9, "69x26x40 in", 73.0, "Truck", true
            ),
            SeedTemplate(
                "2019 Toyota RAV4 XLE AWD - 48,000 Miles", 21500.0,
                "All-wheel drive SUV, Toyota Safety Sense 2.0, blind spot monitor, power liftgate, all-weather floor mats. Clean Carfax.",
                ItemCategory.VEHICLES, ItemCondition.USED_GOOD,
                "3200 Geary Blvd, San Francisco, CA", 37.7810, -122.4540,
                "https://images.unsplash.com/photo-1568605117036-5fe5e7bab0b7?w=800&auto=format&fit=crop&q=80",
                "Hannah Abbott", 4.9, "(555) 098-3456", false, false, 3.6, "181x73x67 in", 3600.0, "Sedan", false
            ),
            SeedTemplate(
                "Rad Power Bikes RadWagon 4 Electric Cargo Bike", 1250.0,
                "Hauls up to 350 lbs payload with long-tail frame. Includes child seats, running boards, and front cargo basket.",
                ItemCategory.VEHICLES, ItemCondition.USED_GOOD,
                "700 Portola Dr, San Francisco, CA", 37.7420, -122.4520,
                "https://images.unsplash.com/photo-1511994298241-608e28f14fde?w=800&auto=format&fit=crop&q=80",
                "Gary Cooper", 4.8, "(555) 987-4567", true, false, 4.5, "79x27x43 in", 76.0, "Truck", true
            ),

            // 46-75: Home & Garden / Furniture
            SeedTemplate(
                "Mid-Century Modern Velvet Sofa (Emerald Green 3-Seater)", 420.0,
                "Stylish 3-seater emerald green velvet couch with tapered brass legs. Very comfortable, high-density foam. Non-smoking home.",
                ItemCategory.HOME_GARDEN, ItemCondition.USED_GOOD,
                "850 Valencia St, San Francisco, CA", 37.7590, -122.4215,
                "https://images.unsplash.com/photo-1555041469-a586c61ea9bc?w=800&auto=format&fit=crop&q=80",
                "Chloe Bennett", 4.9, "(555) 234-9876", true, false, 2.4, "84x35x33 in", 110.0, "Truck", true
            ),
            SeedTemplate(
                "Herman Miller Aeron Ergonomic Office Chair (Size B - Fully Loaded)", 580.0,
                "PostureFit SL back support, fully adjustable arms, forward tilt limiter, quiet rollerblade casters. Pristine condition.",
                ItemCategory.HOME_GARDEN, ItemCondition.USED_LIKE_NEW,
                "400 Montgomery St, San Francisco, CA (Financial District)", 37.7930, -122.4030,
                "https://images.unsplash.com/photo-1580481077195-c3a824555d1a?w=800&auto=format&fit=crop&q=80",
                "Robert Chen", 5.0, "(555) 876-0912", false, false, 0.6, "27x27x41 in", 41.0, "SUV", false
            ),
            SeedTemplate(
                "Breville Barista Touch Espresso Machine (Brushed Stainless)", 620.0,
                "Touchscreen display with automated pre-programmed coffee favorites, ThermoJet 3-second heat up, integrated precision conical burr grinder.",
                ItemCategory.HOME_GARDEN, ItemCondition.USED_LIKE_NEW,
                "1500 Fillmore St, San Francisco, CA", 37.7840, -122.4330,
                "https://images.unsplash.com/photo-1517668808822-9ebb02f2a0e6?w=800&auto=format&fit=crop&q=80",
                "Diana Prince", 4.9, "(555) 765-1023", true, false, 2.1, "13x13x16 in", 23.0, "Sedan", false
            ),
            SeedTemplate(
                "Solid Walnut Live-Edge Dining Table (72\" x 36\") + 6 Chairs", 890.0,
                "Custom hand-crafted kiln-dried American black walnut tabletop with heavy matte black steel U-legs. Includes 6 matching upholstered chairs.",
                ItemCategory.HOME_GARDEN, ItemCondition.USED_LIKE_NEW,
                "2100 Union St, San Francisco, CA (Cow Hollow)", 37.7970, -122.4370,
                "https://images.unsplash.com/photo-1615066390971-03e4e1c36ddf?w=800&auto=format&fit=crop&q=80",
                "Victoria Vance", 5.0, "(555) 654-2134", false, false, 2.8, "72x36x30 in", 160.0, "Truck", true
            ),
            SeedTemplate(
                "Dyson V15 Detect Cordless Vacuum Cleaner with Laser Fluffy Head", 380.0,
                "Reveals invisible microscopic dust on hard floors. Powerful 240AW suction, LCD screen showing particle counts, 60 min runtime.",
                ItemCategory.HOME_GARDEN, ItemCondition.USED_LIKE_NEW,
                "100 Clement St, San Francisco, CA (Inner Richmond)", 37.7830, -122.4600,
                "https://images.unsplash.com/photo-1558317374-067fb5f30001?w=800&auto=format&fit=crop&q=80",
                "Oscar Isaac", 4.8, "(555) 543-3245", true, false, 3.8, "50x10x10 in", 6.8, "Sedan", false
            ),
            SeedTemplate(
                "West Elm Industrial Modular Media Console (68\" Mango Wood)", 340.0,
                "Solid mango wood with blackened steel frame. 2 cabinets with cord cutouts and open shelving for consoles/receivers.",
                ItemCategory.HOME_GARDEN, ItemCondition.USED_GOOD,
                "700 Potrero Ave, San Francisco, CA", 37.7580, -122.4060,
                "https://images.unsplash.com/photo-1595428774223-ef52624120d2?w=800&auto=format&fit=crop&q=80",
                "Lucas Ward", 4.7, "(555) 432-4356", false, false, 2.3, "68x18x24 in", 85.0, "SUV", true
            ),
            SeedTemplate(
                "Monstera Deliciosa & Fiddle Leaf Fig Plant Duo in Ceramic Pots", 75.0,
                "Thriving 4-foot Monstera Deliciosa with large fenestrated leaves and 5-foot Fiddle Leaf Fig in 12\" matte white drainage pots.",
                ItemCategory.HOME_GARDEN, ItemCondition.USED_LIKE_NEW,
                "300 Noe St, San Francisco, CA (Duboce Triangle)", 37.7650, -122.4330,
                "https://images.unsplash.com/photo-1485955900006-10f4d324d411?w=800&auto=format&fit=crop&q=80",
                "Jenna Ortiz", 4.9, "(555) 321-5467", true, false, 2.5, "24x24x50 in", 30.0, "SUV", false
            ),
            SeedTemplate(
                "DeWalt 20V MAX Cordless Drill & Impact Driver Combo Kit", 130.0,
                "Includes DCD771 drill/driver, DCF885 impact driver, two 2.0Ah lithium-ion batteries, fast charger, and heavy-duty tool bag.",
                ItemCategory.HOME_GARDEN, ItemCondition.USED_LIKE_NEW,
                "1500 16th St, San Francisco, CA (Design District)", 37.7660, -122.4000,
                "https://images.unsplash.com/photo-1504148455328-c376907d081c?w=800&auto=format&fit=crop&q=80",
                "Hank Hill", 5.0, "(555) 210-6578", false, false, 1.8, "15x10x6 in", 9.5, "Sedan", false
            ),

            // 76-100: Sporting Goods & Outdoors
            SeedTemplate(
                "Oru Kayak Beach LT Foldable Origami Kayak (12 ft)", 720.0,
                "Folds into compact box size in 3 minutes. Lightweight 25 lbs, holds up to 300 lbs. Includes Oru 4-piece paddle and backpack.",
                ItemCategory.SPORTING_GOODS, ItemCondition.USED_LIKE_NEW,
                "100 Marina Blvd, San Francisco, CA (Marina Safe Exchange)", 37.8040, -122.4380,
                "https://images.unsplash.com/photo-1544551763-46a013bb70d5?w=800&auto=format&fit=crop&q=80",
                "Connor MacLeod", 4.9, "(555) 109-7689", true, false, 3.2, "33x12x29 in (Folded)", 26.0, "Sedan", false
            ),
            SeedTemplate(
                "Bowflex SelectTech 552 Adjustable Dumbbells (Pair) + Stand", 290.0,
                "Adjusts from 5 to 52.5 lbs per dumbbell in 2.5 lb increments with the turn of a dial. Includes ergonomic media rack stand.",
                ItemCategory.SPORTING_GOODS, ItemCondition.USED_LIKE_NEW,
                "500 Parnassus Ave, San Francisco, CA (Inner Sunset)", 37.7630, -122.4570,
                "https://images.unsplash.com/photo-1584735935682-2f2b69dff9d2?w=800&auto=format&fit=crop&q=80",
                "Arnold Strong", 5.0, "(555) 098-8790", false, false, 4.0, "20x15x30 in", 115.0, "SUV", true
            ),
            SeedTemplate(
                "Callaway Paradym Ai Smoke Complete Golf Club Set + Bag", 980.0,
                "Includes 10.5° Driver, 3-Wood, 4-Hybrid, 5-PW Irons, Jaws Raw 56° Wedge, Odyssey Putter, and Callaway Org 14 Cart Bag.",
                ItemCategory.SPORTING_GOODS, ItemCondition.USED_LIKE_NEW,
                "100 Lake Merced Blvd, San Francisco, CA (TPC Harding Park)", 37.7240, -122.4930,
                "https://images.unsplash.com/photo-1535131749006-b7f58c99034b?w=800&auto=format&fit=crop&q=80",
                "Tiger Woods Fan", 4.8, "(555) 987-9801", true, false, 6.2, "48x14x12 in", 22.0, "Sedan", false
            ),
            SeedTemplate(
                "Thule Motion XT XL Rooftop Cargo Box (Titan Gloss)", 520.0,
                "18 cu ft capacity, dual-side opening with SlideLock system, power-click quick mount. Fits skis, snowboards, and family luggage.",
                ItemCategory.SPORTING_GOODS, ItemCondition.USED_LIKE_NEW,
                "1200 4th St, San Francisco, CA (Mission Creek Safe Zone)", 37.7730, -122.3920,
                "https://images.unsplash.com/photo-1506015391300-4802dc74de2e?w=800&auto=format&fit=crop&q=80",
                "Eric Hansen", 4.9, "(555) 876-0912", false, false, 1.7, "84x36x17 in", 46.0, "Truck", true
            ),
            SeedTemplate(
                "YETI Tundra 65 Hard Cooler (Desert Tan)", 240.0,
                "PermaFrost insulation keeps ice for days. FatWall design, BearFoot non-slip feet, heavy-duty rubber T-latches. Clean interior.",
                ItemCategory.SPORTING_GOODS, ItemCondition.USED_GOOD,
                "450 Columbus Ave, San Francisco, CA (North Beach)", 37.7985, -122.4085,
                "https://images.unsplash.com/photo-1527786356703-4b100091cd2c?w=800&auto=format&fit=crop&q=80",
                "Travis Barker", 4.7, "(555) 765-2134", true, false, 1.3, "30x17x16 in", 29.0, "Sedan", false
            ),
            SeedTemplate(
                "Burton Custom X Snowboard 158cm + Step On Genesis Bindings", 480.0,
                "Aggressive carbon-charged camber snowboard with Step On Genesis bindings (Size L) and Burton boot compatibility.",
                ItemCategory.SPORTING_GOODS, ItemCondition.USED_LIKE_NEW,
                "800 Irving St, San Francisco, CA", 37.7635, -122.4660,
                "https://images.unsplash.com/photo-1522056615691-da7b8106829f?w=800&auto=format&fit=crop&q=80",
                "Shaun White Fan", 5.0, "(555) 654-3245", false, false, 4.3, "62x12x6 in", 14.0, "Sedan", false
            ),

            // 101-125: Apparel & Luxury
            SeedTemplate(
                "Canada Goose Expedition Parka Heritage (Men's Large - Black)", 650.0,
                "625 Fill Power responsibly sourced duck down, Arctic tech water-resistant shell, fleece-lined chin guard. Worn for one Alaska trip.",
                ItemCategory.APPAREL, ItemCondition.USED_LIKE_NEW,
                "150 Post St, San Francisco, CA (Union Square Luxury Row)", 37.7885, -122.4050,
                "https://images.unsplash.com/photo-1544441893-675973e31985?w=800&auto=format&fit=crop&q=80",
                "Julian Croft", 5.0, "(555) 543-4356", true, false, 0.7, "24x18x8 in", 4.8, "Sedan", false
            ),
            SeedTemplate(
                "Gucci GG Marmont Small Matelassé Shoulder Bag (Black Leather)", 890.0,
                "Soft structured black matelassé chevron leather with double G hardware. Includes original dustbag, receipt, and authentication card.",
                ItemCategory.APPAREL, ItemCondition.USED_LIKE_NEW,
                "250 Stockton St, San Francisco, CA", 37.7880, -122.4065,
                "https://images.unsplash.com/photo-1584917865442-de89df76afd3?w=800&auto=format&fit=crop&q=80",
                "Vivian Ward", 5.0, "(555) 432-5467", false, false, 0.8, "10x6x3 in", 1.6, "Sedan", false
            ),
            SeedTemplate(
                "Nike Air Jordan 1 Retro High OG 'Chicago Lost & Found' (Size 10.5)", 280.0,
                "Deadstock condition with cracked leather collar vintage aesthetic, original invoice receipt, graphic tissue paper, and sale sticker box.",
                ItemCategory.APPAREL, ItemCondition.NEW,
                "845 Market St, San Francisco, CA (Westfield Center)", 37.7840, -122.4060,
                "https://images.unsplash.com/photo-1552346154-21d32810aba3?w=800&auto=format&fit=crop&q=80",
                "SneakerHead SF", 4.9, "(555) 321-6578", true, true, 0.6, "14x10x5 in", 3.2, "Sedan", false
            ),
            SeedTemplate(
                "Rolex Datejust 36mm Ref 126234 (Blue Motif Dial / Jubilee)", 8400.0,
                "Fluted white gold bezel on stainless steel jubilee bracelet. Complete set with box, green warranty card (2023), tags, and links.",
                ItemCategory.APPAREL, ItemCondition.USED_LIKE_NEW,
                "100 Grant Ave, San Francisco, CA (Bank Vault Safe Exchange)", 37.7885, -122.4045,
                "https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?w=800&auto=format&fit=crop&q=80",
                "Arthur Pendelton", 5.0, "(555) 210-7689", false, false, 0.7, "8x6x4 in", 2.0, "Sedan", false
            ),

            // 126-150: Verified Brand New Items (ShopSafe Storefront Hub)
            SeedTemplate(
                "Brand New ShopSafe Smart Air Fryer XL (7 Quart)", 79.99,
                "Unopened box. ShopSafe verified new merchandise. 12 digital presets, rapid crisp air technology, dishwasher safe basket.",
                ItemCategory.NEW_ITEMS, ItemCondition.NEW,
                "ShopSafe Local Storefront Hub #1 - 100 Mission St", 37.7920, -122.3950,
                "https://images.unsplash.com/photo-1621972750749-0fbb1abb7736?w=800&auto=format&fit=crop&q=80",
                "ShopSafe Official Storefront", 5.0, "(800) 555-SAFE", false, true, 0.5, "14x12x14 in", 13.5, "Sedan", false
            ),
            SeedTemplate(
                "ShopSafe Pro Ultra Sonic Toothbrush & UV Sanitizer Dock", 39.99,
                "40,000 VPM magnetic levitation motor, 5 cleaning modes, wireless induction charging dock with germicidal UV sanitizer chamber.",
                ItemCategory.NEW_ITEMS, ItemCondition.NEW,
                "ShopSafe Local Storefront Hub #1 - 100 Mission St", 37.7920, -122.3950,
                "https://images.unsplash.com/photo-1559591937-e1104e76a6b5?w=800&auto=format&fit=crop&q=80",
                "ShopSafe Official Storefront", 5.0, "(800) 555-SAFE", false, true, 0.5, "8x4x4 in", 1.2, "Sedan", false
            ),
            SeedTemplate(
                "ShopSafe 4K Security Camera Home System (4-Pack Wire-Free)", 189.99,
                "Solar-powered rechargeable 4K color night vision security cameras with AI vehicle/person detection and local encrypted storage hub.",
                ItemCategory.NEW_ITEMS, ItemCondition.NEW,
                "ShopSafe Local Storefront Hub #1 - 100 Mission St", 37.7920, -122.3950,
                "https://images.unsplash.com/photo-1557597774-9d273605dfa9?w=800&auto=format&fit=crop&q=80",
                "ShopSafe Official Storefront", 5.0, "(800) 555-SAFE", false, true, 0.5, "12x10x6 in", 6.2, "Sedan", false
            ),
            SeedTemplate(
                "ShopSafe HEPA H13 Smart Air Purifier for Large Rooms (1000 sq ft)", 119.99,
                "Filters 99.97% of airborne particles, smoke, pet dander, and odors. Real-time air quality PM2.5 monitor with auto whisper quiet mode.",
                ItemCategory.NEW_ITEMS, ItemCondition.NEW,
                "ShopSafe Local Storefront Hub #1 - 100 Mission St", 37.7920, -122.3950,
                "https://images.unsplash.com/photo-1585771724684-38269d6639fd?w=800&auto=format&fit=crop&q=80",
                "ShopSafe Official Storefront", 5.0, "(800) 555-SAFE", false, true, 0.5, "18x10x10 in", 9.4, "Sedan", false
            )
        )

        // Seed the first hand-crafted templates
        templates.forEachIndexed { index, t ->
            items.add(
                MarketplaceItem(
                    id = "market_item_${index + 1}",
                    title = t.title,
                    price = t.price,
                    description = t.description,
                    category = t.category.name,
                    condition = t.condition.name,
                    pickupLocation = t.pickupLocation,
                    latitude = t.lat,
                    longitude = t.lng,
                    imageUrl = t.imageUrl,
                    sellerName = t.sellerName,
                    sellerRating = t.sellerRating,
                    sellerPhone = t.sellerPhone,
                    isFacebookImported = t.isFacebook,
                    isNewItem = t.isNew,
                    distanceMiles = t.distance,
                    timestamp = currentTime - (index * 720000L),
                    itemDimensions = t.dimensions,
                    itemWeightLbs = t.weightLbs,
                    requiredVehicleType = t.vehicleType,
                    isHeavy = t.isHeavy
                )
            )
        }

        // Generate additional structured catalog up to 250 items with rich variety
        val productCatalog = listOf(
            Triple("Samsung 65\" OLED 4K Smart TV", 850.0, ItemCategory.ELECTRONICS),
            Triple("ASUS ROG Zephyrus G14 Gaming Laptop", 1100.0, ItemCategory.ELECTRONICS),
            Triple("Bose QuietComfort Ultra Earbuds", 199.0, ItemCategory.ELECTRONICS),
            Triple("Garmin Fenix 7X Sapphire Solar GPS Watch", 460.0, ItemCategory.ELECTRONICS),
            Triple("Sonos Arc Premium Smart Soundbar", 540.0, ItemCategory.ELECTRONICS),
            Triple("GoPro HERO12 Black Action Camera Bundle", 290.0, ItemCategory.ELECTRONICS),
            Triple("Logitech MX Master 3S + MX Keys Combo", 130.0, ItemCategory.ELECTRONICS),
            Triple("LG DualUp 28\" Ergonomic SDQHD Monitor", 360.0, ItemCategory.ELECTRONICS),
            Triple("Audio-Technica AT2020USB+ Cardioid Mic", 85.0, ItemCategory.ELECTRONICS),
            Triple("SteelSeries Arctis Nova Pro Wireless Headset", 240.0, ItemCategory.ELECTRONICS),
            Triple("Fender Player Stratocaster Electric Guitar", 520.0, ItemCategory.ELECTRONICS),
            Triple("Yamaha P-125 88-Key Weighted Digital Piano", 480.0, ItemCategory.ELECTRONICS),
            Triple("Elgato Stream Deck XL 32-Key Controller", 160.0, ItemCategory.ELECTRONICS),
            Triple("Philips Hue Gradient Lightstrip 65\" + Bridge", 140.0, ItemCategory.ELECTRONICS),
            Triple("Anker 737 Power Bank (PowerCore 24K)", 79.0, ItemCategory.ELECTRONICS),
            Triple("Razer Blade 15 Advanced Gaming Laptop", 1350.0, ItemCategory.ELECTRONICS),
            Triple("Bowers & Wilkins Px7 S2e Headphones", 270.0, ItemCategory.ELECTRONICS),
            Triple("Fujifilm X-T5 Mirrorless Camera Body", 1250.0, ItemCategory.ELECTRONICS),
            Triple("Marshall Stanmore III Bluetooth Speaker", 280.0, ItemCategory.ELECTRONICS),
            Triple("Apple Mac Studio M2 Max 32GB RAM", 1480.0, ItemCategory.ELECTRONICS),

            Triple("2020 Mazda CX-5 Touring AWD", 19200.0, ItemCategory.VEHICLES),
            Triple("2021 Hyundai Ioniq 5 EV Long Range", 23500.0, ItemCategory.VEHICLES),
            Triple("Cannondale Topstone Carbon Gravel Bike", 1750.0, ItemCategory.VEHICLES),
            Triple("Aventon Aventure.2 All-Terrain Fat Tire E-Bike", 1390.0, ItemCategory.VEHICLES),
            Triple("Apollo City Pro Dual Motor Electric Scooter", 890.0, ItemCategory.VEHICLES),
            Triple("2018 Subaru Outback 2.5i Premium", 15800.0, ItemCategory.VEHICLES),
            Triple("Giant Defy Advanced 2 Carbon Road Bike", 1420.0, ItemCategory.VEHICLES),
            Triple("Yamaha MT-07 Motorcycle (Under 5k mi)", 5900.0, ItemCategory.VEHICLES),
            Triple("Juiced RipCurrent S Electric Commuter Bike", 1150.0, ItemCategory.VEHICLES),
            Triple("Kuat NV 2.0 2-Bike Hitch Rack", 480.0, ItemCategory.VEHICLES),

            Triple("Eames Style Lounge Chair & Ottoman (Walnut)", 680.0, ItemCategory.HOME_GARDEN),
            Triple("Article Sven Charme Tan Leather Sofa", 850.0, ItemCategory.HOME_GARDEN),
            Triple("Uplift V2 Commercial Standing Desk 60x30", 430.0, ItemCategory.HOME_GARDEN),
            Triple("Le Creuset Enameled Cast Iron Dutch Oven 5.5 Qt", 220.0, ItemCategory.HOME_GARDEN),
            Triple("KitchenAid Artisan 5-Quart Stand Mixer (Pistachio)", 260.0, ItemCategory.HOME_GARDEN),
            Triple("Vitamix A3500 Ascent Series Smart Blender", 380.0, ItemCategory.HOME_GARDEN),
            Triple("iRobot Roomba Combo j9+ Robot Vacuum & Mop", 590.0, ItemCategory.HOME_GARDEN),
            Triple("CB2 Drommen Solid Acacia Wood Bed Frame (Queen)", 550.0, ItemCategory.HOME_GARDEN),
            Triple("Traeger Pro 575 Pellet Grill & Smoker with WiFIRE", 490.0, ItemCategory.HOME_GARDEN),
            Triple("Pottery Barn PB Comfort Roll Arm Slipcovered Armchair", 380.0, ItemCategory.HOME_GARDEN),
            Triple("Milwaukee M18 FUEL 6-Tool Cordless Combo Kit", 420.0, ItemCategory.HOME_GARDEN),
            Triple("Stihl MS 271 Farm Boss 20\" Gas Chainsaw", 340.0, ItemCategory.HOME_GARDEN),
            Triple("Weber Genesis II E-315 3-Burner Natural Gas Grill", 490.0, ItemCategory.HOME_GARDEN),
            Triple("Restoration Hardware Style Concrete Fire Pit Table", 580.0, ItemCategory.HOME_GARDEN),
            Triple("Crate & Barrel Big Sur Natural Dining Bench", 280.0, ItemCategory.HOME_GARDEN),

            Triple("Concept2 RowErg Indoor Rowing Machine with PM5", 780.0, ItemCategory.SPORTING_GOODS),
            Triple("Peloton Bike+ with Rotating HD Touchscreen", 950.0, ItemCategory.SPORTING_GOODS),
            Triple("BOTE HD Aero Inflatable Stand Up Paddle Board 11'6\"", 540.0, ItemCategory.SPORTING_GOODS),
            Triple("Big Agnes Copper Spur HV UL2 Ultralight Backpacking Tent", 290.0, ItemCategory.SPORTING_GOODS),
            Triple("Rogue Fitness Monster Lite Power Rack Squat Stand", 590.0, ItemCategory.SPORTING_GOODS),
            Triple("NordicTrack Commercial 1750 Folding Treadmill", 890.0, ItemCategory.SPORTING_GOODS),
            Triple("NRS Otter 130 Whitewater Raft with Oar Frame", 2400.0, ItemCategory.SPORTING_GOODS),
            Triple("Penn Battle III Inshore Spinning Fishing Rod & Reel Combo", 130.0, ItemCategory.SPORTING_GOODS),
            Triple("Babolat Pure Drive 2024 Tennis Racket (4 3/8)", 140.0, ItemCategory.SPORTING_GOODS),
            Triple("Shimano Stella SW 8000 Saltwater Offshore Spinning Reel", 680.0, ItemCategory.SPORTING_GOODS),

            Triple("Patagonia Tres 3-in-1 Parka (Men's M - Forge Grey)", 290.0, ItemCategory.APPAREL),
            Triple("Arc'teryx Alpha SV Gore-Tex Pro Jacket (Large)", 450.0, ItemCategory.APPAREL),
            Triple("Saint Laurent Tribute Flat Slide Sandals in Smooth Leather", 380.0, ItemCategory.APPAREL),
            Triple("Omega Speedmaster Professional Moonwatch Sapphire Sandwich", 5800.0, ItemCategory.APPAREL),
            Triple("Barbour Classic Beaufort Waxed Cotton Jacket (Size 40)", 240.0, ItemCategory.APPAREL),
            Triple("Ray-Ban Polarized Wayfarer Classic Sunglasses", 95.0, ItemCategory.APPAREL),
            Triple("Lululemon ABC Classic-Fit Pant 32x32 (Bundle of 3)", 150.0, ItemCategory.APPAREL),
            Triple("Tumi Alpha 3 Continental Dual Access 4-Wheeled Carry-On", 460.0, ItemCategory.APPAREL),
            Triple("Prada Re-Nylon Black Triangle Logo Backpack", 980.0, ItemCategory.APPAREL),
            Triple("Red Wing Heritage Iron Ranger 8111 Boots (Size 10D)", 210.0, ItemCategory.APPAREL),

            Triple("ShopSafe Smart Induction Cooktop Single Burner", 59.99, ItemCategory.NEW_ITEMS),
            Triple("ShopSafe Ultra-Quiet 3-Speed Pedestal Tower Fan", 49.99, ItemCategory.NEW_ITEMS),
            Triple("ShopSafe 10,000mAh Magnetic Wireless Power Bank", 29.99, ItemCategory.NEW_ITEMS),
            Triple("ShopSafe Stainless Steel 10-Piece Kitchen Knife Block Set", 89.99, ItemCategory.NEW_ITEMS),
            Triple("ShopSafe Premium Insulated Thermal Delivery Backpack", 44.99, ItemCategory.NEW_ITEMS),
            Triple("ShopSafe Smart Video Doorbell with 2K HDR & Two-Way Audio", 79.99, ItemCategory.NEW_ITEMS),
            Triple("ShopSafe Ergonomic Memory Foam Lumbar Support Cushion", 24.99, ItemCategory.NEW_ITEMS),
            Triple("ShopSafe Compact Car Jump Starter & Tire Inflator 3000A", 69.99, ItemCategory.NEW_ITEMS),
            Triple("ShopSafe Automatic Smart Pet Feeder with HD Camera", 64.99, ItemCategory.NEW_ITEMS),
            Triple("ShopSafe 65W GaN Fast Charger 3-Port Desktop Station", 34.99, ItemCategory.NEW_ITEMS)
        )

        val sfLocations = listOf(
            Pair("742 Market St, San Francisco, CA (Safe Exchange Zone)", Pair(37.7880, -122.4075)),
            Pair("500 Howard St, San Francisco, CA (Lobby Pick Up)", Pair(37.7895, -122.3990)),
            Pair("1200 Van Ness Ave, San Francisco, CA", Pair(37.7850, -122.4210)),
            Pair("850 Valencia St, San Francisco, CA (Mission District)", Pair(37.7590, -122.4215)),
            Pair("201 3rd St, San Francisco, CA (SOMA Safe Zone)", Pair(37.7858, -122.4011)),
            Pair("1000 Chestnut St, San Francisco, CA (Russian Hill)", Pair(37.8020, -122.4210)),
            Pair("350 Bay St, San Francisco, CA (North Beach)", Pair(37.8055, -122.4135)),
            Pair("650 Townsend St, San Francisco, CA (Design District)", Pair(37.7712, -122.4042)),
            Pair("1500 California St, San Francisco, CA (Nob Hill)", Pair(37.7910, -122.4190)),
            Pair("400 Castro St, San Francisco, CA", Pair(37.7610, -122.4350)),
            Pair("100 Marina Blvd, San Francisco, CA (Marina Green)", Pair(37.8040, -122.4380)),
            Pair("300 4th St, San Francisco, CA (Metreon Exchange)", Pair(37.7830, -122.4030)),
            Pair("550 16th St, San Francisco, CA (Mission Bay)", Pair(37.7670, -122.3920)),
            Pair("1800 Polk St, San Francisco, CA", Pair(37.7935, -122.4225)),
            Pair("1250 Folsom St, San Francisco, CA", Pair(37.7750, -122.4110)),
            Pair("2200 Fillmore St, San Francisco, CA (Pacific Heights)", Pair(37.7890, -122.4340)),
            Pair("1400 Haight St, San Francisco, CA (Haight-Ashbury)", Pair(37.7695, -122.4470)),
            Pair("100 Mission St, San Francisco, CA (ShopSafe Hub #1)", Pair(37.7920, -122.3950))
        )

        val sellerPool = listOf(
            Pair("Elena Vance", 4.9),
            Pair("Marcus Brody", 5.0),
            Pair("Jason Kim", 4.8),
            Pair("Chloe Bennett", 4.9),
            Pair("David Ross", 4.7),
            Pair("Rachel Zhang", 5.0),
            Pair("Kevin Patel", 4.9),
            Pair("Alex Rivera", 4.8),
            Pair("Samantha Reed", 4.9),
            Pair("Tyler Woods", 4.7),
            Pair("Liam O'Connor", 5.0),
            Pair("Brandon Miller", 4.9),
            Pair("Maya Lin", 4.9),
            Pair("Christopher Nolan", 4.8),
            Pair("Jordan Bell", 5.0),
            Pair("Emily Watson", 4.9),
            Pair("ShopSafe Verified Hub", 5.0),
            Pair("Nathan Drake", 4.9),
            Pair("Sophia Martinez", 4.8),
            Pair("Lucas Meyer", 4.9)
        )

        val imageUrlsByCategory = mapOf(
            ItemCategory.ELECTRONICS to listOf(
                "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=800&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=800&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1606813907291-d86edd9b94ad?w=800&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1587202372775-e229f172b9d7?w=800&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=800&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1593784991095-a205069470b6?w=800&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1590602847861-f357a9332bbc?w=800&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1610945415295-d9bbf067e59c?w=800&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=800&auto=format&fit=crop&q=80"
            ),
            ItemCategory.VEHICLES to listOf(
                "https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?w=800&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1560958089-b8a1929cea89?w=800&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1571068316344-75bc76f77890?w=800&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1558981403-c5f9899a28bc?w=800&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1485965120184-e220f721d03e?w=800&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1568605117036-5fe5e7bab0b7?w=800&auto=format&fit=crop&q=80"
            ),
            ItemCategory.HOME_GARDEN to listOf(
                "https://images.unsplash.com/photo-1555041469-a586c61ea9bc?w=800&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1580481077195-c3a824555d1a?w=800&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1517668808822-9ebb02f2a0e6?w=800&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1615066390971-03e4e1c36ddf?w=800&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1558317374-067fb5f30001?w=800&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1504148455328-c376907d081c?w=800&auto=format&fit=crop&q=80"
            ),
            ItemCategory.SPORTING_GOODS to listOf(
                "https://images.unsplash.com/photo-1544551763-46a013bb70d5?w=800&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1584735935682-2f2b69dff9d2?w=800&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1535131749006-b7f58c99034b?w=800&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1506015391300-4802dc74de2e?w=800&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1522056615691-da7b8106829f?w=800&auto=format&fit=crop&q=80"
            ),
            ItemCategory.APPAREL to listOf(
                "https://images.unsplash.com/photo-1544441893-675973e31985?w=800&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1584917865442-de89df76afd3?w=800&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1552346154-21d32810aba3?w=800&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?w=800&auto=format&fit=crop&q=80"
            ),
            ItemCategory.NEW_ITEMS to listOf(
                "https://images.unsplash.com/photo-1621972750749-0fbb1abb7736?w=800&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1559591937-e1104e76a6b5?w=800&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1557597774-9d273605dfa9?w=800&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1585771724684-38269d6639fd?w=800&auto=format&fit=crop&q=80"
            )
        )

        // Expand until we reach exactly 250 items
        var itemCounter = items.size + 1
        var catalogIndex = 0

        while (items.size < 250) {
            val templateItem = productCatalog[catalogIndex % productCatalog.size]
            val cat = templateItem.third
            val loc = sfLocations[items.size % sfLocations.size]
            val seller = sellerPool[items.size % sellerPool.size]
            val imgList = imageUrlsByCategory[cat] ?: imageUrlsByCategory[ItemCategory.ELECTRONICS]!!
            val img = imgList[items.size % imgList.size]

            val cond = when ((items.size + catalogIndex) % 4) {
                0 -> ItemCondition.NEW
                1 -> ItemCondition.USED_LIKE_NEW
                2 -> ItemCondition.USED_GOOD
                else -> ItemCondition.USED_FAIR
            }

            val isHeavy = when (cat) {
                ItemCategory.VEHICLES -> false
                ItemCategory.HOME_GARDEN -> (items.size % 2 == 0)
                ItemCategory.SPORTING_GOODS -> (items.size % 3 == 0)
                else -> false
            }

            val vehicleReq = when {
                isHeavy -> "Truck"
                cat == ItemCategory.VEHICLES -> "Sedan"
                cat == ItemCategory.HOME_GARDEN && items.size % 2 == 1 -> "SUV"
                cat == ItemCategory.SPORTING_GOODS && items.size % 2 == 1 -> "SUV"
                else -> "Sedan"
            }

            val priceVariation = (templateItem.second * (0.85 + ((items.size % 7) * 0.05))).let {
                Math.round(it * 100.0) / 100.0
            }

            val isFb = (items.size % 3 != 0)
            val isNew = (cond == ItemCondition.NEW || cat == ItemCategory.NEW_ITEMS)

            val titleSuffix = if (items.size >= 70) " #${(items.size / 65) + 1} (Edition ${items.size % 10 + 1})" else ""
            val fullTitle = "${templateItem.first}$titleSuffix"

            items.add(
                MarketplaceItem(
                    id = "market_item_$itemCounter",
                    title = fullTitle,
                    price = priceVariation,
                    description = "Verified listing for $fullTitle. Clean inspection, safe public pickup location available, test on arrival. Instant ShopSafe courier delivery eligible.",
                    category = cat.name,
                    condition = cond.name,
                    pickupLocation = loc.first,
                    latitude = loc.second.first,
                    longitude = loc.second.second,
                    imageUrl = img,
                    sellerName = seller.first,
                    sellerRating = seller.second,
                    sellerPhone = "(555) ${100 + (itemCounter % 899)}-${1000 + (itemCounter * 17) % 8999}",
                    isFacebookImported = isFb,
                    isNewItem = isNew,
                    distanceMiles = Math.round((0.4 + ((itemCounter % 40) * 0.12)) * 10.0) / 10.0,
                    timestamp = currentTime - (itemCounter * 360000L),
                    itemDimensions = if (isHeavy) "48x36x30 in" else "14x10x6 in",
                    itemWeightLbs = if (isHeavy) 65.0 + (itemCounter % 45) else 4.5 + (itemCounter % 8),
                    requiredVehicleType = vehicleReq,
                    isHeavy = isHeavy
                )
            )

            itemCounter++
            catalogIndex++
        }

        return items
    }
}
