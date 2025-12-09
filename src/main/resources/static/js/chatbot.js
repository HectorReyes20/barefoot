const chatbotToggle = document.getElementById('chatbotToggle');
const chatbotWindow = document.getElementById('chatbotWindow');
const chatbotClose = document.getElementById('chatbotClose');
const chatbotMessages = document.getElementById('chatbotMessages');
const chatbotInput = document.getElementById('chatbotInput');
const chatbotSend = document.getElementById('chatbotSend');

const responses = {
    'hola': '¡Hola! 😊 Soy tu asistente de Arilu Store. Estoy aquí para ayudarte con cualquier duda sobre nuestras colecciones de ropa.',
    'arilu': 'Arilu Store es tu tienda de ropa de mujer con las últimas tendencias. Ofrecemos:\n• Prendas de alta calidad\n• Diseños modernos y exclusivos\n• Tallas variadas (XS-XXXL)\n• Envíos rápidos y seguros 👗',
    'talla': 'Guía de Tallas 📏\n\nXS: 32-34 | S: 34-36 | M: 36-38 | L: 38-40 | XL: 40-42 | XXL: 42-44 | XXXL: 44+\n\n¿Cómo medir correctamente?\n1. Mide tu busto en cm\n2. Consulta la guía en cada prenda\n3. Si dudas, elige la talla mayor\n\n¿Necesitas ayuda? 👚',
    'envio': 'Información de Envíos 📦\n\n🚚 Bogotá: 24-48 horas\n🇨🇴 Ciudades principales: 3-5 días\n✅ Envío GRATIS en pedidos +$150.000\n📍 Seguimiento en tiempo real\n\n¿Tu ciudad?',
    'devolucion': 'Política de Devoluciones 🔄\n\n✅ 30 días para devoluciones\n✅ Prenda sin usar, con etiquetas\n✅ Reembolso o cambio por otra talla\n✅ Envío de devolución gratis\n\n¿Necesitas iniciar una devolución?',
    'pago': 'Métodos de Pago 💳\n\n✅ Tarjetas de crédito/débito\n✅ Transferencia bancaria\n✅ Nequi / Daviplata\n✅ PayPal\n✅ Pago contra entrega (Bogotá)\n\n¡Todos los pagos son seguros!',
    'cuidado': 'Cuidado de tu Ropa 🧼\n\n• Lee la etiqueta de cuidado\n• Lava con agua fría o tibia\n• Usa detergente suave\n• Seca al aire libre\n• Plancha a temperatura media si necesario\n\n¿Material específico?',
    'garantia': 'Garantía de Calidad ✨\n\n✅ Prendas de primera calidad\n✅ Materiales certificados\n✅ Garantía en defectos de fabricación\n\n¿Tienes una prenda con defecto?',
    'colecciones': 'Nuestras Colecciones 👗\n\n👔 Casual - Cómoda y versátil\n💼 Formal - Para ocasiones especiales\n🏃‍♀️ Deportiva - Activa y moderna\n🌙 Básicos - Essentials que no faltan\n✨ Premium - Edición limitada\n\n¿Cuál te interesa?',
    'contacto': 'Contáctanos 📱\n\nWhatsApp: +57 300 1234567\nEmail: hola@arilutore.com\nInstagram: @AriluStore\n\n¿Deseas que te contactemos?',
    'stock': 'Para verificar stock:\n1. Ve al producto\n2. Selecciona tu talla\n3. Verás disponibilidad en tiempo real\n\n¿Buscas algún modelo? 🔍',
    'seguimiento': 'Seguimiento de Pedido 📍\n\nIngresa tu código de pedido para rastrear tu compra en tiempo real.',
    'descuentos': 'Promociones y Descuentos 🎉\n\n✨ Suscríbete a nuestro newsletter para ofertas exclusivas\n🎁 Promociones semanales\n💝 Descuentos por volumen\n\n¿Quieres conocer nuestras ofertas?',
    'talles': 'Dudas sobre Tallas? 📐\n\nOfrecemos tallas para todas: XS, S, M, L, XL, XXL, XXXL\n\n¿Cuál es tu talla habitual?',
    'gracias': '¡De nada! 😊 ¿Algo más?',
    'adios': '¡Hasta pronto! 👋',
    'ayuda': 'Puedo ayudarte con preguntas sobre tallas, envíos, devoluciones, cuidado, colecciones y más.',
    'default': 'No entendí muy bien 🤔. Pregúntame sobre tallas, envíos, devoluciones o nuestras colecciones.'
};

// Menú principal
const mainMenuOptions = [
    { text: '📏 ¿Qué talla necesito?', action: 'talla' },
    { text: '📦 Información de envíos', action: 'envio' },
    { text: '🔄 Devoluciones y cambios', action: 'devolucion' },
    { text: '👗 Nuestras colecciones', action: 'colecciones' },
    { text: '📍 Seguir mi pedido', action: 'seguimiento_pedido' },
    { text: '❓ Más preguntas frecuentes', action: 'ayuda' }
];

// Menús contextuales
const contextMenus = {
    'ayuda': [
        { text: '💳 Métodos de pago', action: 'pago' },
        { text: '🧼 Cuidado de la ropa', action: 'cuidado' },
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
    'envio_bogota': 'Envíos en Bogotá: 24-48 horas, $20.000 o gratis desde $150.000.',
    'envio_ciudades': 'Envíos a ciudades principales: 3-5 días hábiles.',
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
        'Ingresa tu código de pedido de compra (ej: ARD-2025-001234)',
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
            whatsappBtn.href = 'https://wa.me/573001234567?text=Hola%20Arilu%20Store%20quiero%20conocer%20el%20estado%20de%20mi%20pedido';
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
        '¡Hola! Soy el Asistente de Arilu Store. ¿En qué puedo ayudarte? 👗',
        'bot'
    );
}, 1000);