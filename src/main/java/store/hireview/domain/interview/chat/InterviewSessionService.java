package store.hireview.domain.interview.chat;

import reactor.core.publisher.Flux;
import store.hireview.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class InterviewSessionService {
    private final InterviewSessionRepository sessionRepository;
    private final UserService userService;
    private final ChatService chatService;

    public Mono<InterviewSession> createSession(String email, CreateSessionRequestDTO req) {
        return userService.findUserByEmail(email)
                .flatMap(user -> {
                    InterviewSession sessionEntity = new InterviewSession(user.getId(), req);

                    return sessionRepository.save(sessionEntity)
                            .flatMap(session -> {
                                if (session.getSessionType() == SessionType.INTERVIEWER) {
                                    return sendInterviewerIntroMessages(
                                            session.getLanguage(),
                                            session.getSessionId(),
                                            session.getJobTitle()
                                    )
                                            .then(Mono.just(session));
                                } else {
                                    return chatService.loadQuestionsForSession(session.getSessionId(), req)
                                            .thenMany(sendLocalizedIntroMessages(session.getLanguage(), user.getName(), session.getSessionId()))
                                            .thenMany(chatService.getNextQuestion(session.getSessionId(), 0))
                                            .then(Mono.just(session));
                                }
                            });
                });
    }


    private Flux<ChatMessage> sendLocalizedIntroMessages(String language, String name, String sessionId) {
        String langKey = language == null ? "english" : language.toLowerCase();
        String greeting = String.format(
                GREETING_MAP.getOrDefault(langKey, GREETING_MAP.get("english")),
                name
        );
        String startMessage = START_MESSAGE_MAP.getOrDefault(langKey, START_MESSAGE_MAP.get("english"));

        return Flux.concat(
                chatService.saveMessage(sessionId, "AI", greeting),
                chatService.saveMessage(sessionId, "AI", startMessage)
        );
    }

    private Flux<ChatMessage> sendInterviewerIntroMessages(String language, String sessionId, String jobTitle) {
        String langKey = language == null ? "english" : language.toLowerCase();

        String line1 = String.format(
                INTRO_LINE1_MAP.getOrDefault(langKey, INTRO_LINE1_MAP.get("english")),
                jobTitle
        );
        String line2 = INTRO_LINE2_MAP.getOrDefault(langKey, INTRO_LINE2_MAP.get("english"));

        ChatMessage line2Message = new ChatMessage(sessionId, "AI", line2);
        line2Message.setQuestion(true);

        return Flux.concat(
                chatService.saveMessage(sessionId, "AI", line1),
                chatService.saveMessage(line2Message)
        );
    }


    public Mono<Integer> getCurrentStep(String sessionId) {
        return sessionRepository.findBySessionId(sessionId)
                .map(InterviewSession::getCurrentStep)
                .defaultIfEmpty(0);
    }

    public Mono<Void> incrementStep(String sessionId) {
        return sessionRepository.findBySessionId(sessionId)
                .flatMap(session -> {
                    session.incrementStep();
                    return sessionRepository.save(session);
                })
                .then();
    }

    public Mono<InterviewSession> getSession(String sessionId) {
        return sessionRepository.findBySessionId(sessionId);
    }

    private static final Map<String, String> GREETING_MAP = Map.of(
            "english", "Hello, %s. Welcome to Hireview.",
            "korean", "안녕하세요, %s님. 하이어뷰에 오신 것을 환영합니다.",
            "japanese", "こんにちは、%sさん。Hireviewへようこそ。",
            "chinese", "你好，%s。欢迎来到Hireview。",
            "spanish", "Hola, %s. Bienvenido a Hireview.",
            "french", "Bonjour, %s. Bienvenue chez Hireview.",
            "german", "Hallo, %s. Willkommen bei Hireview.",
            "russian", "Здравствуйте, %s. Добро пожаловать в Hireview.",
            "portuguese", "Olá, %s. Bem-vindo ao Hireview.",
            "arabic", "مرحبًا %s. أهلاً بك في Hireview."
    );

    private static final Map<String, String> START_MESSAGE_MAP = Map.of(
            "english", "Let's begin your interview. You will be asked 5 questions.",
            "korean", "지금부터 면접을 시작하겠습니다. 총 5개의 질문이 주어집니다.",
            "japanese", "これから面接を始めます。5つの質問があります。",
            "chinese", "现在开始面试。一共有5个问题。",
            "spanish", "Comencemos la entrevista. Recibirás 5 preguntas.",
            "french", "Commençons l'entretien. Vous aurez 5 questions.",
            "german", "Wir beginnen jetzt das Interview. Es gibt 5 Fragen.",
            "russian", "Начнем интервью. Вам будет задано 5 вопросов.",
            "portuguese", "Vamos começar a entrevista. Serão feitas 5 perguntas.",
            "arabic", "لنبدأ المقابلة. سيتم طرح 5 أسئلة."
    );

    private static final Map<String, String> INTRO_LINE1_MAP = Map.of(
            "english", "Hello interviewer, I applied for the %s position.",
            "korean", "안녕하세요 면접관님. 저는 %s 직무에 지원한 지원자입니다.",
            "japanese", "こんにちは、面接官様。%s職に応募させていただきました。",
            "chinese", "面试官您好，我是应聘 %s 职位的候选人。",
            "spanish", "Hola entrevistador, me postulé para el puesto de %s.",
            "french", "Bonjour recruteur, j'ai postulé au poste de %s.",
            "german", "Hallo Interviewer, ich habe mich für die Stelle als %s beworben.",
            "russian", "Здравствуйте, я подал заявку на позицию %s.",
            "portuguese", "Olá entrevistador, candidatei-me à vaga de %s.",
            "arabic", "مرحبًا أيها المحاور، لقد تقدمت لوظيفة %s."
    );

    private static final Map<String, String> INTRO_LINE2_MAP = Map.of(
            "english", "I'm excited to begin. Please start whenever you're ready.",
            "korean", "이 자리에 오게 되어 기쁩니다. 언제든지 시작해 주세요.",
            "japanese", "この機会をいただきありがとうございます。いつでも始めてください。",
            "chinese", "我很高兴能参与这次面试。请随时开始。",
            "spanish", "Estoy emocionado de comenzar. Puede iniciar cuando lo desee.",
            "french", "Je suis ravi de commencer. Commencez quand vous êtes prêt.",
            "german", "Ich freue mich, zu beginnen. Starten Sie, wann immer Sie bereit sind.",
            "russian", "Я рад начать. Можете начинать, когда будете готовы.",
            "portuguese", "Estou animado para começar. Pode iniciar quando quiser.",
            "arabic", "أنا متحمس للبدء. يمكنك البدء في أي وقت تشاء."
    );


}
