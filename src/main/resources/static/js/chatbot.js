const chatbotToggle = document.getElementById('chatbotToggle');
const chatbotWindow = document.getElementById('chatbotWindow');
const chatbotClose = document.getElementById('chatbotClose');
const chatbotMessages = document.getElementById('chatbotMessages');
const chatbotInput = document.getElementById('chatbotInput');
const chatbotSend = document.getElementById('chatbotSend');

const responses = {
    'hola': '¡Hola! 😊 Soy tu asistente de Barefoot Store. Estoy aquí para ayudarte con cualquier duda sobre nuestros calzados: zapatillas, botas, sandalias y más. 👟',
    'barefoot': 'Barefoot Store es tu tienda de calzado con estilo y comodidad. Ofrecemos:\n• Modelos para hombre y mujer\n• Desde casual hasta deportivo y formal\n• Tallas completas y medias tallas (según modelo)\n• Envíos rápidos y cambios fáciles ✅',
    'talla': 'Guía de Tallas de Calzado 📏\n\nCómo elegir tu talla:\n1) Pon tu pie sobre una hoja y marca talón y punta\n2) Mide la distancia en cm\n3) Compara con la guía del producto\n\nReferencia (aprox.):\n• 22.5 cm ≈ 35\n• 23.5 cm ≈ 36\n• 24.0 cm ≈ 37\n• 24.5 cm ≈ 38\n• 25.5 cm ≈ 39\n• 26.0 cm ≈ 40\n• 26.5 cm ≈ 41\n• 27.0 cm ≈ 42\n• 27.5 cm ≈ 43\n• 28.0 cm ≈ 44\n\nSi dudas entre dos tallas, te sugerimos la mayor.',
    'envio': 'Información de Envíos 📦\n\n🚚 Lima Metropolitana: 24-48 horas\n📦 Provincias: 2-5 días hábiles\n✅ Envío GRATIS desde S/400\n📍 Seguimiento del pedido disponible\n\n¿Desde qué ciudad nos escribes?',
    'devolucion': 'Cambios y Devoluciones 🔄\n\n✅ 15 días para cambios\n✅ El calzado debe estar sin uso, en su caja original\n✅ Cambios por talla o modelo (según stock)\n✅ Reembolso en compras online según políticas\n\n¿Deseas iniciar un cambio?',
    'pago': 'Métodos de Pago 💳\n\n✅ Tarjetas de crédito/débito\n✅ Yape / Transferencia\n✅ Pago en efectivo (según cobertura)\n✅ Stripe para pagos online\n\nTodos los pagos son seguros.',
    'cuidado': 'Cuidado del Calzado 🧼\n\n• No lavar zapatillas en lavadora\n• Para cuero: limpiar con paño ligeramente húmedo y usar crema especial\n• Para gamuza/serraje: cepillo suave y protector para repelencia\n• Secar a la sombra, nunca al sol directo\n• Usar hormas o papel para conservar la forma\n\n¿Material específico?',
    'garantia': 'Garantía de Calidad ✨\n\n✅ Garantía por defectos de fabricación\n✅ Materiales y acabados de primera\n✅ Revisión técnica en caso de reclamos\n\n¿Tuviste algún inconveniente con un par?',
    'colecciones': 'Nuestras Colecciones 👟\n\n👟 Casual - Para el día a día\n🏃‍♂️ Running - Amortiguación y ligereza\n🥾 Outdoor - Trekking y aventura\n👞 Formal - Elegancia y confort\n🩴 Sandalias - Frescas y cómodas\n\n¿Qué estilo estás buscando?',
    'contacto': 'Contáctanos 📱\n\nWhatsApp: +51 997 256 008\nEmail: soporte@barefoot-store.com\nInstagram: @BarefootStore\n\n¿Deseas que te contactemos?',
    'stock': 'Para verificar stock:\n1) Entra al producto\n2) Selecciona tu talla\n3) Verás disponibilidad en tiempo real\n\n¿Buscas algún modelo o talla en particular? 🔍',
    'seguimiento': 'Seguimiento de Pedido 📍\n\nIngresa tu código de pedido para rastrear tu compra en tiempo real.',
    'descuentos': 'Promociones y Descuentos 🎉\n\n✨ Únete a nuestro newsletter para ofertas exclusivas\n🎁 Promos semanales\n💝 Descuentos por volumen\n\n¿Quieres ver las ofertas vigentes?',
    'talles': 'Dudas sobre Tallas? 📐\n\nTe ayudamos a elegir la talla ideal según la longitud de tu pie en cm. ¿Cuál es tu medida?',
    'gracias': '¡De nada! 😊 ¿Algo más?',
    'adios': '¡Hasta pronto! 👋',
    'ayuda': 'Puedo ayudarte con preguntas sobre tallas, envíos, devoluciones, cuidado, colecciones y más.',
    'default': 'No entendí muy bien 🤔. Pregúntame sobre tallas, envíos, devoluciones o nuestros modelos de calzado.'
};

// Menú principal
const mainMenuOptions = [
    { text: '📏 ¿Qué talla necesito?', action: 'talla' },
    { text: '📦 Información de envíos', action: 'envio' },
    { text: '🔄 Cambios y devoluciones', action: 'devolucion' },
    { text: '👟 Nuestras colecciones', action: 'colecciones' },
    { text: '📍 Seguir mi pedido', action: 'seguimiento_pedido' },
    { text: '❓ Más preguntas frecuentes', action: 'ayuda' }
];

// Menús contextuales
const contextMenus = {
    'ayuda': [
        { text: '💳 Métodos de pago', action: 'pago' },
        { text: '🧼 Cuidado del calzado', action: 'cuidado' },
        { text: '✨ Garantía y calidad', action: 'garantia' },
        { text: '🎉 Promociones', action: 'descuentos' },
        { text: '📱 Contacto directo', action: 'contacto' },
        { text: '🔙 Volver al menú principal', action: 'menu' }
    ],
    'seguimiento_pedido': [
        { text: '🔙 Volver al menú', action: 'menu' }
    ]
};

// Respuestas detalladas
const detailedResponses = {
    'menu': '¡Perfecto! ¿Qué necesitas saber? 😊',
    'envio_lima': 'Envíos en Lima: 24-48 horas. Envío gratis desde S/199.',
    'envio_provincias': 'Provincias: 2-5 días hábiles. Revisa tu tracking para más detalles.',
    'seguimiento': 'Para rastrear tu pedido ingresa tu código de compra.'
};

// abrir/cerrar chatbot
chatbotToggle.addEventListener('click', () => {
    chatbotWindow.classList.toggle('active');
    if (chatbotWindow.classList.contains('active')) chatbotInput.focus();
});

chatbotClose.addEventListener('click', () => chatbotWindow.classList.remove('active'));

// Enviar mensaje
function sendMessage() {
    const message = chatbotInput.value.trim();
    if (message === '') return;

    addMessage(message, 'user', false);
    chatbotInput.value = '';

    setTimeout(() => {
        const response = getBotResponse(message);
        const matchedKey = getMatchedKey(message);
        addMessage(response, 'bot', true, matchedKey);
    }, 600);
}

function addMessage(text, sender, showOptions = true, contextKey = null) {
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${sender}`;

    const contentDiv = document.createElement('div');
    contentDiv.className = 'message-content';
    contentDiv.textContent = text;

    messageDiv.appendChild(contentDiv);
    chatbotMessages.appendChild(messageDiv);
    chatbotMessages.scrollTop = chatbotMessages.scrollHeight;

    if (sender === 'bot' && showOptions) {
        setTimeout(() => showOptionsAfterMessage(contextKey), 300);
    }
}

function getMatchedKey(message) {
    const lower = message.toLowerCase();
    return Object.keys(responses).find(key => lower.includes(key)) || null;
}

function showOptionsAfterMessage(contextKey) {
    let options = mainMenuOptions;
    if (contextKey && contextMenus[contextKey]) options = contextMenus[contextKey];
    addOptionButtons(options);
}

function addOptionButtons(options) {
    const wrapper = document.createElement('div');
    wrapper.className = 'message bot';

    const box = document.createElement('div');
    box.className = 'chatbot-options';

    const title = document.createElement('div');
    title.className = 'options-title';
    title.textContent = '¿Cómo te puedo ayudar?';
    box.appendChild(title);

    options.forEach(option => {
        const btn = document.createElement('button');
        btn.className = 'chatbot-option-btn';
        btn.textContent = option.text;
        btn.onclick = () => handleOptionClick(option.action, option.text);
        box.appendChild(btn);
    });

    wrapper.appendChild(box);
    chatbotMessages.appendChild(wrapper);
    chatbotMessages.scrollTop = chatbotMessages.scrollHeight;
}

function handleOptionClick(action, text) {
    addMessage(text, 'user', false);

    document.querySelectorAll('.chatbot-options').forEach(opt => opt.remove());

    setTimeout(() => {
        if (action === 'seguimiento_pedido') {
            handleOrderTracking();
        } else {
            let response = detailedResponses[action] || responses[action] || responses['default'];
            let nextContext = action in detailedResponses ? action : action;
            addMessage(response, 'bot', true, nextContext);
        }
    }, 600);
}

// Nueva función para manejar seguimiento de pedido
function handleOrderTracking() {
    addMessage(
        'Ingresa tu código de pedido de compra (ej: BFT-2025-001234)',
        'bot',
        false
    );

    // Crear input para código de pedido
    const wrapper = document.createElement('div');
    wrapper.className = 'message bot';

    const inputContainer = document.createElement('div');
    inputContainer.className = 'order-tracking-input';

    const input = document.createElement('input');
    input.type = 'text';
    input.placeholder = 'Código del pedido...';
    input.className = 'tracking-input';

    const btn = document.createElement('button');
    btn.className = 'tracking-btn';
    btn.textContent = 'Rastrear';
    btn.onclick = () => validateOrderCode(input.value, wrapper);

    inputContainer.appendChild(input);
    inputContainer.appendChild(btn);
    wrapper.appendChild(inputContainer);
    chatbotMessages.appendChild(wrapper);
    chatbotMessages.scrollTop = chatbotMessages.scrollHeight;

    input.focus();
}

// Nueva función para validar código de pedido
function validateOrderCode(code, inputWrapper) {
    if (!code.trim()) {
        addMessage('Por favor ingresa un código válido.', 'bot', false);
        return;
    }

    // Simulación de validación (conceptual)
    inputWrapper.remove();
    addMessage(`Validando código: ${code}...`, 'user', false);

    setTimeout(() => {
        // Validación exitosa (conceptual)
        addMessage(
            `✅ ¡Validación exitosa! Tu pedido ${code} está siendo procesado.\n\nPuedes comunicarte con nosotros para más detalles:`,
            'bot',
            false
        );

        // Crear botón de WhatsApp
        setTimeout(() => {
            const wrapper = document.createElement('div');
            wrapper.className = 'message bot';

            const whatsappBtn = document.createElement('a');
            whatsappBtn.href = 'https://wa.me/51900000000?text=Hola%20Barefoot%20Store%20quiero%20conocer%20el%20estado%20de%20mi%20pedido';
            whatsappBtn.target = '_blank';
            whatsappBtn.className = 'whatsapp-btn';
            whatsappBtn.textContent = '📱 Chatear por WhatsApp';

            wrapper.appendChild(whatsappBtn);
            chatbotMessages.appendChild(wrapper);
            chatbotMessages.scrollTop = chatbotMessages.scrollHeight;

            // Mostrar menú principal después
            setTimeout(() => {
                addMessage('¿Hay algo más en lo que pueda ayudarte?', 'bot', true, 'menu');
            }, 1000);
        }, 800);
    }, 1500);
}

function getBotResponse(message) {
    const lower = message.toLowerCase();
    return Object.keys(responses).find(key => lower.includes(key))
        ? responses[Object.keys(responses).find(key => lower.includes(key))]
        : responses['default'];
}

// Mensaje de bienvenida
setTimeout(() => {
    addMessage(
        '¡Hola! Soy el Asistente de Barefoot Store. ¿En qué puedo ayudarte? 👟',
        'bot'
    );
}, 1000);