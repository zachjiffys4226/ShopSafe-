package com.example.shopsafe.data.util

enum class AppLanguage(
    val code: String,
    val nativeName: String,
    val flagEmoji: String,
    val englishName: String
) {
    ENGLISH("en", "English", "🇺🇸", "English"),
    SPANISH("es", "Español", "🇲🇽", "Spanish"),
    FRENCH("fr", "Français", "🇫🇷", "French"),
    GERMAN("de", "Deutsch", "🇩🇪", "German"),
    CHINESE("zh", "中文", "🇨🇳", "Chinese"),
    PORTUGUESE("pt", "Português", "🇧🇷", "Portuguese");

    companion object {
        fun fromCode(code: String): AppLanguage {
            return values().find { it.code.equals(code, ignoreCase = true) } ?: ENGLISH
        }
    }
}

object LocalizedStrings {
    fun get(key: String, language: AppLanguage): String {
        return translations[key]?.get(language)
            ?: translations[key]?.get(AppLanguage.ENGLISH)
            ?: key
    }

    private val translations: Map<String, Map<AppLanguage, String>> = mapOf(
        "profile_settings" to mapOf(
            AppLanguage.ENGLISH to "Profile & Settings",
            AppLanguage.SPANISH to "Perfil y Configuración",
            AppLanguage.FRENCH to "Profil et Paramètres",
            AppLanguage.GERMAN to "Profil & Einstellungen",
            AppLanguage.CHINESE to "个人资料与设置",
            AppLanguage.PORTUGUESE to "Perfil e Configurações"
        ),
        "app_language" to mapOf(
            AppLanguage.ENGLISH to "App Language & Localization",
            AppLanguage.SPANISH to "Idioma de la App y Localización",
            AppLanguage.FRENCH to "Langue de l'App et Localisation",
            AppLanguage.GERMAN to "App-Sprache & Lokalisierung",
            AppLanguage.CHINESE to "应用语言与本地化",
            AppLanguage.PORTUGUESE to "Idioma do App e Localização"
        ),
        "select_language_subtitle" to mapOf(
            AppLanguage.ENGLISH to "Select your preferred language for ShopSafe UI & real-time notifications",
            AppLanguage.SPANISH to "Selecciona tu idioma preferido para la interfaz y notificaciones en tiempo real",
            AppLanguage.FRENCH to "Sélectionnez votre langue préférée pour l'interface et les notifications",
            AppLanguage.GERMAN to "Wählen Sie Ihre bevorzugte Sprache für Benutzeroberfläche und Benachrichtigungen",
            AppLanguage.CHINESE to "选择您偏好的 ShopSafe 界面与实时通知语言",
            AppLanguage.PORTUGUESE to "Selecione seu idioma preferido para a interface e notificações em tempo real"
        ),
        "driver_mode" to mapOf(
            AppLanguage.ENGLISH to "Driver Mode",
            AppLanguage.SPANISH to "Modo Conductor",
            AppLanguage.FRENCH to "Mode Chauffeur",
            AppLanguage.GERMAN to "Fahrermodus",
            AppLanguage.CHINESE to "司机模式",
            AppLanguage.PORTUGUESE to "Modo Motorista"
        ),
        "driver_mode_desc" to mapOf(
            AppLanguage.ENGLISH to "View live map & current location to wait for nearby order pings",
            AppLanguage.SPANISH to "Ver mapa en vivo y ubicación para recibir ofertas cercanas",
            AppLanguage.FRENCH to "Voir la carte en direct et l'emplacement pour recevoir les commandes",
            AppLanguage.GERMAN to "Live-Karte und Standort anzeigen, um auf Bestellungen zu warten",
            AppLanguage.CHINESE to "查看实时地图与当前位置以接收附近订单通知",
            AppLanguage.PORTUGUESE to "Ver mapa ao vivo e localização para aguardar ofertas de entrega"
        ),
        "account_details" to mapOf(
            AppLanguage.ENGLISH to "Account Details",
            AppLanguage.SPANISH to "Detalles de la Cuenta",
            AppLanguage.FRENCH to "Détails du Compte",
            AppLanguage.GERMAN to "Kontodetails",
            AppLanguage.CHINESE to "账户详情",
            AppLanguage.PORTUGUESE to "Detalhes da Conta"
        ),
        "account_name" to mapOf(
            AppLanguage.ENGLISH to "Account Name",
            AppLanguage.SPANISH to "Nombre de la Cuenta",
            AppLanguage.FRENCH to "Nom du Compte",
            AppLanguage.GERMAN to "Kontoname",
            AppLanguage.CHINESE to "账户姓名",
            AppLanguage.PORTUGUESE to "Nome da Conta"
        ),
        "membership_status" to mapOf(
            AppLanguage.ENGLISH to "Membership Status",
            AppLanguage.SPANISH to "Estado de Membresía",
            AppLanguage.FRENCH to "Statut de Membre",
            AppLanguage.GERMAN to "Mitgliedschaftsstatus",
            AppLanguage.CHINESE to "会员状态",
            AppLanguage.PORTUGUESE to "Status de Membro"
        ),
        "stripe_payouts" to mapOf(
            AppLanguage.ENGLISH to "Stripe Payouts",
            AppLanguage.SPANISH to "Pagos con Stripe",
            AppLanguage.FRENCH to "Paiements Stripe",
            AppLanguage.GERMAN to "Stripe-Auszahlungen",
            AppLanguage.CHINESE to "Stripe 提现账户",
            AppLanguage.PORTUGUESE to "Pagamentos Stripe"
        ),
        "saved_addresses" to mapOf(
            AppLanguage.ENGLISH to "Saved Delivery Addresses",
            AppLanguage.SPANISH to "Direcciones de Entrega Guardadas",
            AppLanguage.FRENCH to "Adresses de Livraison Enregistrées",
            AppLanguage.GERMAN to "Gespeicherte Lieferadressen",
            AppLanguage.CHINESE to "已保存的送货地址",
            AppLanguage.PORTUGUESE to "Endereços de Entrega Salvos"
        ),
        "saved_addresses_desc" to mapOf(
            AppLanguage.ENGLISH to "Cached locally with Jetpack DataStore for instant checkout",
            AppLanguage.SPANISH to "Guardado localmente con Jetpack DataStore para pago rápido",
            AppLanguage.FRENCH to "Enregistré localement avec Jetpack DataStore pour un paiement instantané",
            AppLanguage.GERMAN to "Lokal mit Jetpack DataStore gespeichert für schnellen Checkout",
            AppLanguage.CHINESE to "通过 Jetpack DataStore 本地缓存以实现即时结账",
            AppLanguage.PORTUGUESE to "Armazenado localmente com Jetpack DataStore para checkout instantâneo"
        ),
        "marketplace" to mapOf(
            AppLanguage.ENGLISH to "Marketplace",
            AppLanguage.SPANISH to "Mercado",
            AppLanguage.FRENCH to "Marché",
            AppLanguage.GERMAN to "Marktplatz",
            AppLanguage.CHINESE to "集市",
            AppLanguage.PORTUGUESE to "Mercado"
        ),
        "food_stores" to mapOf(
            AppLanguage.ENGLISH to "Food & Stores",
            AppLanguage.SPANISH to "Comida y Tiendas",
            AppLanguage.FRENCH to "Nourriture et Magasins",
            AppLanguage.GERMAN to "Essen & Geschäfte",
            AppLanguage.CHINESE to "美食与商家",
            AppLanguage.PORTUGUESE to "Comida e Lojas"
        ),
        "messages" to mapOf(
            AppLanguage.ENGLISH to "Messages",
            AppLanguage.SPANISH to "Mensajes",
            AppLanguage.FRENCH to "Messages",
            AppLanguage.GERMAN to "Nachrichten",
            AppLanguage.CHINESE to "消息",
            AppLanguage.PORTUGUESE to "Mensagens"
        ),
        "orders" to mapOf(
            AppLanguage.ENGLISH to "Orders",
            AppLanguage.SPANISH to "Pedidos",
            AppLanguage.FRENCH to "Commandes",
            AppLanguage.GERMAN to "Bestellungen",
            AppLanguage.CHINESE to "订单",
            AppLanguage.PORTUGUESE to "Pedidos"
        ),
        "cart" to mapOf(
            AppLanguage.ENGLISH to "Cart",
            AppLanguage.SPANISH to "Carrito",
            AppLanguage.FRENCH to "Panier",
            AppLanguage.GERMAN to "Warenkorb",
            AppLanguage.CHINESE to "购物车",
            AppLanguage.PORTUGUESE to "Carrinho"
        ),
        "done" to mapOf(
            AppLanguage.ENGLISH to "Done",
            AppLanguage.SPANISH to "Listo",
            AppLanguage.FRENCH to "Terminé",
            AppLanguage.GERMAN to "Fertig",
            AppLanguage.CHINESE to "完成",
            AppLanguage.PORTUGUESE to "Concluído"
        ),
        "search_placeholder" to mapOf(
            AppLanguage.ENGLISH to "Search items, groceries, or local stores...",
            AppLanguage.SPANISH to "Buscar artículos, víveres o tiendas locales...",
            AppLanguage.FRENCH to "Rechercher des articles, épicerie ou magasins...",
            AppLanguage.GERMAN to "Artikel, Lebensmittel oder Geschäfte suchen...",
            AppLanguage.CHINESE to "搜索商品、生鲜杂货或本地商家...",
            AppLanguage.PORTUGUESE to "Buscar itens, mercadorias ou lojas locais..."
        ),
        "open_ml_dispatcher" to mapOf(
            AppLanguage.ENGLISH to "Open ML Smart Order Dispatcher",
            AppLanguage.SPANISH to "Abrir Despachador Inteligente ML",
            AppLanguage.FRENCH to "Ouvrir le Dispatcher Intelligent ML",
            AppLanguage.GERMAN to "KI Smart Order Dispatcher öffnen",
            AppLanguage.CHINESE to "打开 ML 智能订单派单系统",
            AppLanguage.PORTUGUESE to "Abrir Despachador Inteligente ML"
        ),
        "save" to mapOf(
            AppLanguage.ENGLISH to "Save",
            AppLanguage.SPANISH to "Guardar",
            AppLanguage.FRENCH to "Enregistrer",
            AppLanguage.GERMAN to "Speichern",
            AppLanguage.CHINESE to "保存",
            AppLanguage.PORTUGUESE to "Salvar"
        ),
        "add_address_placeholder" to mapOf(
            AppLanguage.ENGLISH to "Add new delivery address...",
            AppLanguage.SPANISH to "Agregar nueva dirección de entrega...",
            AppLanguage.FRENCH to "Ajouter une nouvelle adresse...",
            AppLanguage.GERMAN to "Neue Lieferadresse hinzufügen...",
            AppLanguage.CHINESE to "添加新送货地址...",
            AppLanguage.PORTUGUESE to "Adicionar novo endereço de entrega..."
        )
    )
}
