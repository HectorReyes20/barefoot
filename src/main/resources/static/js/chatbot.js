// Chatbot de Soporte y Asistencia al Cliente - Barefoot Store
const chatbotToggle = document.getElementById('chatbotToggle');
const chatbotWindow = document.getElementById('chatbotWindow');
const chatbotClose = document.getElementById('chatbotClose');
const chatbotMessages = document.getElementById('chatbotMessages');
const chatbotInput = document.getElementById('chatbotInput');
const chatbotSend = document.getElementById('chatbotSend');

const responses = {
    'hola': '¡Hola! 😊 Soy tu asistente de soporte. Estoy aquí para ayudarte con cualquier duda sobre nuestros productos y servicios.',
    'barefoot': 'El calzado barefoot imita caminar descalzo, respetando la anatomía del pie. Características:\n• Suela flexible y delgada\n• Puntera amplia (no aprieta los dedos)\n• Cero drop (sin desnivel talón-punta)\n• Ligero y flexible 👣',
    'talla': 'Manejamos tallas desde la 35 hasta la 45. Para elegir tu talla correcta:\n\n1. Mide tu pie en cm (del talón a la punta del dedo más largo)\n2. Consulta nuestra guía de tallas en la página del producto\n3. Si estás entre dos tallas, elige la mayor\n\n¿Necesitas ayuda para medir tu pie? 📏',
    'envio': 'Información de Envíos 📦\n\n🚚 Lima: Entrega en 24-48 horas\n🇵🇪 Provincias: 3-5 días hábiles\n✅ Envío GRATIS en Lima para compras +S/ 400\n📍 Seguimiento en tiempo real\n\n¿Tienes una dirección específica?',
    'devolucion': 'Política de Devoluciones 🔄\n\n✅ 30 días para devoluciones\n✅ Producto sin usar, con etiquetas\n✅ Reembolso o cambio por otra talla/modelo\n✅ El envío de devolución corre por nuestra cuenta\n\n¿Necesitas iniciar una devolución?',
    'pago': 'Métodos de Pago 💳\n\n✅ Tarjetas de crédito/débito\n✅ Transferencia bancaria\n✅ Yape / Plin\n✅ Pago contra entrega (solo Lima)\n\n¡Todos los pagos son seguros!',
    'cuidado': 'Cuidado del Calzado Barefoot 🧼\n\n• Limpia con un paño húmedo\n• No uses lavadora\n• Seca al aire libre\n• No lo expongas al sol directo\n• Usa protector para cuero si aplica\n\n¿Tienes algún material específico?',
    'garantia': 'Garantía y Calidad ✨\n\n✅ 6 meses de garantía\n✅ Materiales certificados\n✅ Fabricación artesanal peruana\n\n¿Tienes un problema con tu producto?',
    'personalizar': 'Personalización de Calzado 🎨\n\nPuedes elegir colores, materiales y más.\n\n¡Ve a la sección "Personalizar" para crear tu diseño!',
    'tiempo': 'Tiempos de Fabricación ⏱️\n\n📦 Stock: envío inmediato\n🎨 Personalizado: 7-10 días hábiles\n✨ Diseños especiales: 10-15 días\n\n¿Lo necesitas urgente?',
    'categoria': 'Categorías Disponibles 👟\n\nCasual, Deportivo, Formal, Senderismo y Running.\n\n¿Qué categoría te interesa?',
    'contacto': 'Contáctanos 📱\n\nWhatsApp: +51 922 928 818\nEmail: contacto@barefootstore.pe\nTienda en Lima\n\n¿Deseas que te contactemos?',
    'stock': 'Para verificar stock:\n1. Ve al producto\n2. Mira el stock en tiempo real\n\n¿Buscas algún modelo?',
    'adaptacion': 'Adaptación al Barefoot 🦶\n\nUsa 1-2h al inicio y aumenta gradualmente.\n¿Sientes molestias?',
    'beneficios': 'Beneficios 💪\n\nMejora postura, equilibrio, fuerza en los pies.\n¿Tienes un problema podológico?',
    'gracias': '¡De nada! 😊 ¿Algo más?',
    'adios': '¡Hasta pronto! 👋',
    'ayuda': 'Puedo ayudarte con preguntas frecuentes, envíos, devoluciones, cuidado, tallas y más.',
    'default': 'No entendí muy bien 🤔. Pregúntame sobre tallas, envíos, devoluciones o personalización.'
};

// Menú principal
const mainMenuOptions = [
    { text: '📏 ¿Qué talla necesito?', action: 'talla' },
    { text: '📦 Información de envíos', action: 'envio' },
    { text: '🔄 Devoluciones y cambios', action: 'devolucion' },
    { text: '👟 ¿Qué es calzado barefoot?', action: 'barefoot' },
    { text: '❓ Más preguntas frecuentes', action: 'ayuda' }
];

// Menús contextuales
const contextMenus = {
    'ayuda': [
        { text: '💳 Métodos de pago', action: 'pago' },
        { text: '⏱️ Tiempos de fabricación', action: 'tiempo' },
        { text: '🧼 Cuidado del calzado', action: 'cuidado' },
        { text: '✨ Garantía y calidad', action: 'garantia' },
        { text: '📱 Contacto directo', action: 'contacto' },
        { text: '🔙 Volver al menú principal', action: 'menu' }
    ]
};

// Respuestas detalladas
const detailedResponses = {
    'menu': '¡Perfecto! ¿Qué necesitas saber? 😊',
    'envio_lima': 'Envíos en Lima: 24-48 horas, S/15 o gratis desde S/400.',
    'envio_provincias': 'Envíos a provincias: 3-5 días hábiles.',
    'seguimiento': 'Para rastrear tu pedido ingresa tu número de pedido.'
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
        let response = detailedResponses[action] || responses[action] || responses['default'];
        let nextContext = action in detailedResponses ? action : action;
        addMessage(response, 'bot', true, nextContext);
    }, 600);
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
        '¡Hola! Soy el Asistente de Barefoot Store. ¿En qué puedo ayudarte? 😊',
        'bot'
    );
}, 1000);
