// Chatbot JavaScript
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
        'pago': 'Métodos de Pago 💳\n\n✅ Tarjetas de crédito/débito (Visa, Mastercard)\n✅ Transferencia bancaria\n✅ Yape / Plin\n✅ Pago contra entrega (solo Lima)\n\n¡Todos los pagos son seguros!',
        'cuidado': 'Cuidado del Calzado Barefoot 🧼\n\n• Limpia con un paño húmedo\n• No uses lavadora\n• Seca al aire libre (no al sol directo)\n• Guarda en lugar fresco y seco\n• Usa protector para cuero si aplica\n\n¿Tienes algún material específico?',
        'garantia': 'Garantía y Calidad ✨\n\n✅ 6 meses de garantía contra defectos de fabricación\n✅ Materiales certificados de primera calidad\n✅ Fabricación artesanal peruana\n✅ Control de calidad en cada par\n\n¿Tienes un problema con tu producto?',
        'personalizar': 'Personalización de Calzado 🎨\n\n✅ Elige colores (cuerpo, suela, cordones)\n✅ Selecciona materiales (cuero, lona, malla)\n✅ Diseños únicos y exclusivos\n✅ Sin costo adicional en colores\n\n¡Ve a la sección "Personalizar" para crear tu diseño!',
        'tiempo': 'Tiempos de Fabricación ⏱️\n\n📦 Productos en stock: Envío inmediato\n🎨 Productos personalizados: 7-10 días hábiles\n✨ Diseños especiales: 10-15 días hábiles\n\n¿Necesitas tu pedido urgente?',
        'categoria': 'Categorías Disponibles 👟\n\n🚶 Casual - Uso diario\n🏃 Deportivo - Actividad física\n👔 Formal - Trabajo y eventos\n⛰️ Senderismo - Aventuras outdoor\n🏃‍♂️ Running - Para corredores\n\n¿Qué categoría te interesa?',
        'contacto': 'Contáctanos 📱\n\n📧 Email: contacto@barefootstore.pe\n📱 WhatsApp: +51 922 928 818\n🏪 Tienda: Av. Principal 123, Villa María del Triunfo, Lima\n🕐 Horario: Lunes a Sábado 9AM - 7PM\n\n¿Prefieres que te contactemos?',
        'stock': 'Para verificar el stock de un producto específico:\n\n1. Ve a la página del producto que te interesa\n2. Verás el stock disponible en tiempo real\n3. Si dice "Stock bajo", ¡apresúrate!\n\n¿Buscas algún modelo en particular?',
        'adaptacion': 'Adaptación al Calzado Barefoot 🦶\n\n⚠️ Importante: Transición gradual\n\n• Semana 1-2: Usa 1-2 horas al día\n• Semana 3-4: Aumenta a 3-4 horas\n• Semana 5+: Uso completo\n\n¡Tu pie necesita fortalecerse! ¿Tienes molestias?',
        'beneficios': 'Beneficios del Barefoot 💪\n\n✅ Fortalece músculos del pie\n✅ Mejora postura y equilibrio\n✅ Reduce dolor de espalda\n✅ Mayor sensibilidad y conexión con el suelo\n✅ Previene lesiones\n\n¿Tienes algún problema podológico específico?',
        'gracias': '¡De nada! 😊 Estoy aquí para ayudarte. ¿Hay algo más en lo que pueda asistirte?',
        'adios': '¡Hasta pronto! 👋 Si necesitas más ayuda, aquí estaré. ¡Que tengas un excelente día!',
        'ayuda': 'Puedo ayudarte con:\n\n❓ Preguntas frecuentes\n📏 Guía de tallas\n📦 Envíos y entregas\n🔄 Devoluciones\n🎨 Personalización\n💳 Métodos de pago\n🦶 Cuidado y adaptación\n📱 Contacto directo\n\n¿Qué necesitas saber?',
        'default': 'Hmm, no estoy seguro de entender tu pregunta. 🤔\n\nPuedes preguntarme sobre:\n• Tallas y medidas\n• Envíos\n• Devoluciones\n• Cuidado del calzado\n• Personalización\n\nO escribe "ayuda" para ver todas las opciones.\n\n¿En qué puedo ayudarte? 😊'
    };

    // Menú principal de soporte
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
        ],
        'talla': [
            { text: '📐 ¿Cómo medir mi pie?', action: 'medir_pie' },
            { text: '👶 Tallas para niños', action: 'tallas_ninos' },
            { text: '📊 Ver tabla de tallas', action: 'tabla_tallas' },
            { text: '🔙 Volver al menú principal', action: 'menu' }
        ],
        'barefoot': [
            { text: '💪 Beneficios para la salud', action: 'beneficios' },
            { text: '🦶 ¿Cómo adaptarme?', action: 'adaptacion' },
            { text: '👟 Categorías disponibles', action: 'categoria' },
            { text: '🔙 Volver al menú principal', action: 'menu' }
        ],
        'envio': [
            { text: '🚚 Envíos a Lima', action: 'envio_lima' },
            { text: '🇵🇪 Envíos a provincias', action: 'envio_provincias' },
            { text: '📍 Seguimiento de pedido', action: 'seguimiento' },
            { text: '🔙 Volver al menú principal', action: 'menu' }
        ],
        'devolucion': [
            { text: '🔄 Iniciar devolución', action: 'iniciar_devolucion' },
            { text: '↔️ Cambio de talla', action: 'cambio_talla' },
            { text: '📋 Requisitos', action: 'requisitos_devolucion' },
            { text: '🔙 Volver al menú principal', action: 'menu' }
        ],
        'contacto': [
            { text: '📱 WhatsApp directo', action: 'whatsapp' },
            { text: '📧 Enviar email', action: 'email' },
            { text: '🏪 Visitar tienda física', action: 'tienda' },
            { text: '🔙 Volver al menú principal', action: 'menu' }
        ]
    };

    // Respuestas detalladas
    const detailedResponses = {
        'menu': '¡Perfecto! Estoy aquí para ayudarte. ¿Qué necesitas saber? 😊',
        'medir_pie': 'Cómo Medir tu Pie Correctamente 📐\n\n1️⃣ Coloca una hoja en el suelo contra la pared\n2️⃣ Para sobre la hoja con el talón pegado a la pared\n3️⃣ Marca donde llega tu dedo más largo\n4️⃣ Mide la distancia en centímetros\n5️⃣ Agrega 0.5-1cm de holgura\n\n💡 Tip: Mide ambos pies, usa la medida del más grande\n\n¿Necesitas ayuda para elegir tu talla?',
        'tallas_ninos': 'Tallas para Niños 👶\n\nDisponibles desde la talla 25 hasta la 34.\n\n⚠️ Importante:\n• Los niños crecen rápido, deja 1-1.5cm de holgura\n• Verifica la medida cada 3-4 meses\n• El barefoot es excelente para el desarrollo del pie infantil\n\n¿Necesitas ayuda con una talla específica?',
        'tabla_tallas': 'Tabla de Tallas 📊\n\nMedida del pie → Talla\n\n35: 22-22.5 cm\n36: 22.5-23 cm\n37: 23-23.5 cm\n38: 23.5-24 cm\n39: 24-24.5 cm\n40: 24.5-25 cm\n41: 25-25.5 cm\n42: 25.5-26 cm\n43: 26-26.5 cm\n44: 26.5-27 cm\n45: 27-27.5 cm\n\n¿Tienes tu medida en cm?',
        'envio_lima': 'Envíos en Lima Metropolitana 🚚\n\n✅ Entrega: 24-48 horas\n✅ Costo: S/ 15\n✅ GRATIS en compras +S/ 400\n✅ Seguimiento en tiempo real\n✅ Pago contra entrega disponible\n\nZonas de cobertura: Todos los distritos\n\n¿Cuál es tu distrito?',
        'envio_provincias': 'Envíos a Provincias 🇵🇪\n\n✅ Cobertura: Todo el Perú\n✅ Tiempo: 3-5 días hábiles\n✅ Costo: Desde S/ 15 (varía según zona)\n✅ Agencias: Olva Courier, Shalom\n✅ Seguimiento incluido\n\n¿De qué ciudad eres?',
        'seguimiento': 'Seguimiento de Pedido 📍\n\nPara rastrear tu pedido necesitas:\n\n1️⃣ Número de pedido (te llegó por email)\n2️⃣ Ingresa a nuestra web → "Rastrear Pedido"\n3️⃣ O escríbenos por WhatsApp con tu número de pedido\n\n¿Tienes tu número de pedido?',
        'iniciar_devolucion': 'Iniciar Devolución 🔄\n\nPasos:\n\n1️⃣ Escríbenos a contacto@barefootstore.pe o WhatsApp\n2️⃣ Indica tu número de pedido y motivo\n3️⃣ Empacamos y recogemos el producto (sin costo)\n4️⃣ Reembolso en 5-7 días hábiles\n\n¿Necesitas ayuda para contactarnos?',
        'cambio_talla': 'Cambio de Talla ↔️\n\n✅ Sin costo adicional\n✅ Enviamos la nueva talla\n✅ Recogemos la que no te quedó\n✅ Proceso: 3-5 días\n\nEscríbenos a:\n📱 WhatsApp: +51 922 928 818\n📧 Email: contacto@barefootstore.pe\n\n¿Qué talla necesitas?',
        'requisitos_devolucion': 'Requisitos para Devolución 📋\n\n✅ Dentro de los 30 días de compra\n✅ Producto sin usar\n✅ Con etiquetas originales\n✅ En su empaque original\n✅ Comprobante de compra\n\n❌ No aplica para productos personalizados\n\n¿Tu producto cumple los requisitos?',
        'whatsapp': 'WhatsApp Directo 📱\n\n¡Escríbenos ahora!\n\n+51 922 928 818\n\nHorario de atención:\nLunes a Sábado: 9AM - 7PM\n\n💬 Respuesta en minutos\n\n¿Quieres que te comparta el enlace directo?',
        'email': 'Email de Soporte 📧\n\ncontacto@barefootstore.pe\n\n⏱️ Tiempo de respuesta: Menos de 24 horas\n\n¿Sobre qué tema quieres escribirnos?',
        'tienda': 'Visítanos 🏪\n\n📍 Dirección:\nAv. Principal 123, Urb. El Artesano\nVilla María del Triunfo, Lima\n\n🕐 Horario:\nLunes a Sábado: 9AM - 7PM\n\n✨ En tienda puedes:\n• Probarte todos los modelos\n• Recibir asesoría personalizada\n• Ver materiales y acabados\n• Crear diseños personalizados\n\n¿Quieres las indicaciones para llegar?'
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
            setTimeout(() => {
                showOptionsAfterMessage(contextKey);
            }, 300);
        }
    }

    function getMatchedKey(message) {
        const lowerMessage = message.toLowerCase();
        for (const key in responses) {
            if (lowerMessage.includes(key)) {
                return key;
            }
        }
        return null;
    }

    function showOptionsAfterMessage(contextKey) {
        let optionsToShow = mainMenuOptions;

        if (contextKey && contextMenus[contextKey]) {
            optionsToShow = contextMenus[contextKey];
        }

        addOptionButtons(optionsToShow);
    }

    function addOptionButtons(options) {
        const optionsContainer = document.createElement('div');
        optionsContainer.className = 'message bot';

        const optionsWrapper = document.createElement('div');
        optionsWrapper.className = 'chatbot-options';

        const optionsTitle = document.createElement('div');
        optionsTitle.className = 'options-title';
        optionsTitle.textContent = '¿Cómo te puedo ayudar?';
        optionsWrapper.appendChild(optionsTitle);

        options.forEach(option => {
            const button = document.createElement('button');
            button.className = 'chatbot-option-btn';
            button.textContent = option.text;
            button.onclick = () => handleOptionClick(option.action, option.text);
            optionsWrapper.appendChild(button);
        });

        optionsContainer.appendChild(optionsWrapper);
        chatbotMessages.appendChild(optionsContainer);
        chatbotMessages.scrollTop = chatbotMessages.scrollHeight;
    }

    function handleOptionClick(action, buttonText) {
        addMessage(buttonText, 'user', false);

        const allOptions = document.querySelectorAll('.chatbot-options');
        allOptions.forEach(opt => opt.remove());

        setTimeout(() => {
            let response;
            let nextContext = null;

            if (detailedResponses[action]) {
                response = detailedResponses[action];
                nextContext = determineNextContext(action);
            } else if (responses[action]) {
                response = responses[action];
                nextContext = action;
            } else {
                response = responses['default'];
                nextContext = null;
            }

            addMessage(response, 'bot', true, nextContext);
        }, 600);
    }

    function determineNextContext(action) {
        if (action === 'menu') return null;

        if (['medir_pie', 'tallas_ninos', 'tabla_tallas'].includes(action)) return 'talla';
        if (['envio_lima', 'envio_provincias', 'seguimiento'].includes(action)) return 'envio';
        if (['iniciar_devolucion', 'cambio_talla', 'requisitos_devolucion'].includes(action)) return 'devolucion';
        if (['whatsapp', 'email', 'tienda'].includes(action)) return 'contacto';
        if (['beneficios', 'adaptacion', 'categoria'].includes(action)) return 'barefoot';

        return action;
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



    setTimeout(() => {
        addMessage('¡Hola! Soy el Asistente de Soporte de Barefoot Store. Estoy aquí para resolver tus dudas sobre tallas, envíos, devoluciones y más. ¿En qué puedo ayudarte? 😊',
        'bot');