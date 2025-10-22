// Chatbot JavaScript
    const chatbotToggle = document.getElementById('chatbotToggle');
    const chatbotWindow = document.getElementById('chatbotWindow');
    const chatbotClose = document.getElementById('chatbotClose');
    const chatbotMessages = document.getElementById('chatbotMessages');
    const chatbotInput = document.getElementById('chatbotInput');
    const chatbotSend = document.getElementById('chatbotSend');

    const responses = {
        'hola': '¡Hola! 😊 Bienvenido a Barefoot Store. ¿En qué puedo ayudarte?',
        'barefoot': 'El calzado barefoot imita caminar descalzo, respetando la anatomía del pie. Tiene suela flexible, amplia puntera y cero drop. ¡Es muy saludable! 👣',
        'precio': 'Nuestros precios van desde S/ 299 hasta S/ 399:\n• Urban Barefoot: S/ 299\n• Sport Flex: S/ 349\n• Classic Barefoot: S/ 399 💰',
        'envio': 'Hacemos envíos a todo el Perú 🇵🇪 En Lima el envío es GRATIS para compras mayores a S/ 200. 📦',
        'personalizar': 'Puedes personalizar tu calzado eligiendo colores, materiales y detalles únicos. Ve a la sección "Personalizar" en el menú. 🎨',
        'talla': 'Manejamos tallas desde la 35 hasta la 45. Te recomendamos consultar nuestra guía de tallas. 📏',
        'tami': '¡Tamara Alexandra te amo demasiadoo <3!',
        'pago': 'Aceptamos tarjetas de crédito/débito, transferencias bancarias y pago contra entrega en Lima. 💳',
        'contacto': '📧 Email: contacto@barefootstore.pe\n📱 WhatsApp: +51 922 928 818',
        'horario': 'Atendemos de lunes a sábado de 9:00 AM - 7:00 PM 🕐',
        'gracias': '¡De nada! 😊 ¿Hay algo más en lo que pueda ayudarte?',
        'adios': '¡Hasta pronto! 👋 Gracias por visitar Barefoot Store.',
        'ayuda': 'Puedo ayudarte con:\n• Información sobre barefoot 👟\n• Precios 💰\n• Envíos 📦\n• Personalización 🎨\n• Contacto 📱',
        'default': 'Interesante pregunta. Para más información contáctanos:\n📱 WhatsApp: +51 922 928 818\n📧 contacto@barefootstore.pe 😊'
    };

    chatbotToggle.addEventListener('click', () => {
        chatbotWindow.classList.toggle('active');
        if (chatbotWindow.classList.contains('active')) {
            chatbotInput.focus();
        }
    });

    chatbotClose.addEventListener('click', () => {
        chatbotWindow.classList.remove('active');
    });

    function sendMessage() {
        const message = chatbotInput.value.trim();
        if (message === '') return;

        addMessage(message, 'user');
        chatbotInput.value = '';

        setTimeout(() => {
            const response = getBotResponse(message);
            addMessage(response, 'bot');
        }, 600);
    }

    function addMessage(text, sender) {
        const messageDiv = document.createElement('div');
        messageDiv.className = `message ${sender}`;

        const contentDiv = document.createElement('div');
        contentDiv.className = 'message-content';
        contentDiv.textContent = text;

        messageDiv.appendChild(contentDiv);
        chatbotMessages.appendChild(messageDiv);
        chatbotMessages.scrollTop = chatbotMessages.scrollHeight;
    }

    function getBotResponse(message) {
        const lowerMessage = message.toLowerCase();
        for (const key in responses) {
            if (lowerMessage.includes(key)) {
                return responses[key];
            }
        }
        return responses['default'];
    }

    chatbotSend.addEventListener('click', sendMessage);
    chatbotInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            sendMessage();
        }
    });

    setTimeout(() => {
        addMessage('Puedo ayudarte con información sobre productos, precios, envíos y más. ¡Pregúntame! 💬', 'bot');
    }, 1000);