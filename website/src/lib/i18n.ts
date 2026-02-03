export type Language = 'en' | 'de' | 'es' | 'fr';

export const LANGUAGES: { code: Language; label: string }[] = [
	{ code: 'en', label: 'English' },
	{ code: 'de', label: 'Deutsch' },
	{ code: 'es', label: 'Español' },
	{ code: 'fr', label: 'Français' }
];

const translations: Record<Language, Record<string, string>> = {
	en: {
		'nav.home': 'Home',
		'nav.edit': 'Edit',
		'nav.addHabit': 'Add Habit',
		'nav.addCategory': 'Add Category',
		'nav.scoringRules': 'Scoring Rules',
		'nav.logout': 'Logout',
		'home.title': 'Your Habits',
		'home.loading': 'Loading habits...',
		'home.noHabits': 'No habits yet.',
		'home.loadDefaults': 'Load Default Habits',
		'home.loadingDefaults': 'Loading defaults...',
		'home.defaultsLoaded': 'Default habits loaded successfully',
		'home.allCategories': 'All categories',
		'home.complete': 'Complete',
		'home.undo': 'Undo',
		'home.delete': 'Delete',
		'home.confirmDelete': 'Are you sure you want to delete this habit?',
		'home.todayEntries': 'today',
		'home.dailyLimit': 'Daily limit reached',
		'eval.title': 'Evaluation',
		'eval.streak': 'Current streak',
		'eval.longestStreak': 'Longest streak',
		'eval.completionRate': 'Completion rate',
		'eval.completions': 'Completions',
		'eval.target': 'Target',
		'eval.days': 'days',
		'eval.onTrack': 'On track',
		'eval.behind': 'Behind',
		'eval.weeklyScore': 'Weekly Score',
		'eval.currentScore': 'Current',
		'eval.predictedScore': 'Predicted',
		'eval.points': 'pts',
		'eval.avoidanceRate': 'Avoidance rate',
		'eval.avoidanceStreak': 'Avoidance streak'
	},
	de: {
		'nav.home': 'Start',
		'nav.edit': 'Bearbeiten',
		'nav.addHabit': 'Gewohnheit hinzufügen',
		'nav.addCategory': 'Kategorie hinzufügen',
		'nav.scoringRules': 'Bewertungsregeln',
		'nav.logout': 'Abmelden',
		'home.title': 'Deine Gewohnheiten',
		'home.loading': 'Gewohnheiten werden geladen...',
		'home.noHabits': 'Noch keine Gewohnheiten.',
		'home.loadDefaults': 'Standardgewohnheiten laden',
		'home.loadingDefaults': 'Standards werden geladen...',
		'home.defaultsLoaded': 'Standardgewohnheiten erfolgreich geladen',
		'home.allCategories': 'Alle Kategorien',
		'home.complete': 'Erledigt',
		'home.undo': 'Rückgängig',
		'home.delete': 'Löschen',
		'home.confirmDelete': 'Möchtest du diese Gewohnheit wirklich löschen?',
		'home.todayEntries': 'heute',
		'home.dailyLimit': 'Tageslimit erreicht',
		'eval.title': 'Auswertung',
		'eval.streak': 'Aktuelle Serie',
		'eval.longestStreak': 'Längste Serie',
		'eval.completionRate': 'Abschlussrate',
		'eval.completions': 'Abschlüsse',
		'eval.target': 'Ziel',
		'eval.days': 'Tage',
		'eval.onTrack': 'Im Plan',
		'eval.behind': 'Rückstand',
		'eval.weeklyScore': 'Wochenpunktzahl',
		'eval.currentScore': 'Aktuell',
		'eval.predictedScore': 'Voraussichtlich',
		'eval.points': 'Pkt',
		'eval.avoidanceRate': 'Vermeidungsrate',
		'eval.avoidanceStreak': 'Vermeidungsserie'
	},
	es: {
		'nav.home': 'Inicio',
		'nav.edit': 'Editar',
		'nav.addHabit': 'Añadir hábito',
		'nav.addCategory': 'Añadir categoría',
		'nav.scoringRules': 'Reglas de puntuación',
		'nav.logout': 'Cerrar sesión',
		'home.title': 'Tus hábitos',
		'home.loading': 'Cargando hábitos...',
		'home.noHabits': 'Aún no hay hábitos.',
		'home.loadDefaults': 'Cargar hábitos predeterminados',
		'home.loadingDefaults': 'Cargando predeterminados...',
		'home.defaultsLoaded': 'Hábitos predeterminados cargados con éxito',
		'home.allCategories': 'Todas las categorías',
		'home.complete': 'Completar',
		'home.undo': 'Deshacer',
		'home.delete': 'Eliminar',
		'home.confirmDelete': '¿Estás seguro de que quieres eliminar este hábito?',
		'home.todayEntries': 'hoy',
		'home.dailyLimit': 'Límite diario alcanzado',
		'eval.title': 'Evaluación',
		'eval.streak': 'Racha actual',
		'eval.longestStreak': 'Racha más larga',
		'eval.completionRate': 'Tasa de finalización',
		'eval.completions': 'Finalizaciones',
		'eval.target': 'Objetivo',
		'eval.days': 'días',
		'eval.onTrack': 'En camino',
		'eval.behind': 'Atrasado',
		'eval.weeklyScore': 'Puntuación semanal',
		'eval.currentScore': 'Actual',
		'eval.predictedScore': 'Previsto',
		'eval.points': 'pts',
		'eval.avoidanceRate': 'Tasa de evitación',
		'eval.avoidanceStreak': 'Racha de evitación'
	},
	fr: {
		'nav.home': 'Accueil',
		'nav.edit': 'Modifier',
		'nav.addHabit': 'Ajouter habitude',
		'nav.addCategory': 'Ajouter catégorie',
		'nav.scoringRules': 'Règles de notation',
		'nav.logout': 'Déconnexion',
		'home.title': 'Vos habitudes',
		'home.loading': 'Chargement des habitudes...',
		'home.noHabits': "Pas encore d'habitudes.",
		'home.loadDefaults': 'Charger les habitudes par défaut',
		'home.loadingDefaults': 'Chargement des défauts...',
		'home.defaultsLoaded': 'Habitudes par défaut chargées avec succès',
		'home.allCategories': 'Toutes les catégories',
		'home.complete': 'Compléter',
		'home.undo': 'Annuler',
		'home.delete': 'Supprimer',
		'home.confirmDelete': 'Êtes-vous sûr de vouloir supprimer cette habitude ?',
		'home.todayEntries': "aujourd'hui",
		'home.dailyLimit': 'Limite journalière atteinte',
		'eval.title': 'Évaluation',
		'eval.streak': 'Série actuelle',
		'eval.longestStreak': 'Plus longue série',
		'eval.completionRate': "Taux d'achèvement",
		'eval.completions': 'Achèvements',
		'eval.target': 'Objectif',
		'eval.days': 'jours',
		'eval.onTrack': 'En bonne voie',
		'eval.behind': 'En retard',
		'eval.weeklyScore': 'Score hebdomadaire',
		'eval.currentScore': 'Actuel',
		'eval.predictedScore': 'Prévu',
		'eval.points': 'pts',
		'eval.avoidanceRate': "Taux d'évitement",
		'eval.avoidanceStreak': "Série d'évitement"
	}
};

export function getLanguage(): Language {
	if (typeof localStorage !== 'undefined') {
		const saved = localStorage.getItem('language');
		if (saved && (saved === 'en' || saved === 'de' || saved === 'es' || saved === 'fr')) {
			return saved as Language;
		}
	}
	return 'en';
}

export function setLanguage(lang: Language): void {
	localStorage.setItem('language', lang);
}

export function t(key: string, lang: Language): string {
	return translations[lang]?.[key] ?? translations['en']?.[key] ?? key;
}
