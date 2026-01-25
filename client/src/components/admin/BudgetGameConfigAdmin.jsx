import { useState, useEffect } from 'react';
import { budgetGameApi } from '../../services/api.js';
import { useUser } from '../../context/UserContext.jsx';
import { useNavigate } from 'react-router';
import './BudgetGameConfigAdmin.css';

export default function BudgetGameConfigAdmin() {
    const [configs, setConfigs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [showCreateForm, setShowCreateForm] = useState(false);
    const [editingConfigId, setEditingConfigId] = useState(null);
    const [formError, setFormError] = useState('');
    const [successMessage, setSuccessMessage] = useState('');

    const { user, isAuthenticated, loading: authLoading } = useUser();
    const navigate = useNavigate();

    // Check if user is admin
    useEffect(() => {
        if (authLoading) return;

        const isAdmin = user?.role === 'admin'
            || (Array.isArray(user?.roles) && user.roles.includes('admin') && isAuthenticated === true)
            || user?.isAdmin === true;

        if (!isAdmin) {
            navigate('/', { replace: true });
        }
    }, [user, authLoading, navigate]);

    const [formData, setFormData] = useState({
        name: '',
        description: '',
        minSavingsRate: 0.2,
        mealCost: 5,
        minMeals: 25,
        minElectricity: 20,
        minWater: 15,
        minGas: 15,
        minPlayBudget: 500,
        prizePocketMoney: 25,
        eventConfig: ''
    });

    useEffect(() => {
        fetchConfigs();
    }, []);

    async function fetchConfigs() {
        try {
            setLoading(true);
            setError('');
            const data = await budgetGameApi.getAllConfigs();
            setConfigs(data);
        } catch (err) {
            setError(err.message || 'Failed to load configurations');
            console.error(err);
        } finally {
            setLoading(false);
        }
    }

    async function handleSubmit(e) {
        e.preventDefault();
        setFormError('');
        setSuccessMessage('');

        try {
            // Validate event config JSON
            try {
                JSON.parse(formData.eventConfig);
            } catch (err) {
                setFormError('Invalid JSON in event configuration: ' + err.message);
                return;
            }

            if (editingConfigId) {
                await budgetGameApi.updateConfig(editingConfigId, formData);
                setSuccessMessage('Configuration updated successfully!');
            } else {
                await budgetGameApi.createConfig(formData);
                setSuccessMessage('Configuration created successfully!');
            }

            // Reset form and reload configs
            resetForm();
            await fetchConfigs();

            // Clear success message after 3 seconds
            setTimeout(() => setSuccessMessage(''), 3000);
        } catch (err) {
            setFormError(err.message || 'Failed to save configuration');
            console.error(err);
        }
    }

    async function handleDelete(id) {
        if (!window.confirm('Are you sure you want to delete this configuration?')) {
            return;
        }

        try {
            setError('');
            await budgetGameApi.deleteConfig(id);
            setSuccessMessage('Configuration deleted successfully!');
            await fetchConfigs();
            setTimeout(() => setSuccessMessage(''), 3000);
        } catch (err) {
            setError(err.message || 'Failed to delete configuration');
            console.error(err);
        }
    }

    async function handleActivate(id) {
        try {
            setError('');
            await budgetGameApi.setActiveConfig(id);
            setSuccessMessage('Configuration activated successfully!');
            await fetchConfigs();
            setTimeout(() => setSuccessMessage(''), 3000);
        } catch (err) {
            setError(err.message || 'Failed to activate configuration');
            console.error(err);
        }
    }

    async function handleEdit(config) {
        setFormData({
            name: config.name,
            description: config.description,
            minSavingsRate: config.minSavingsRate,
            mealCost: config.mealCost,
            minMeals: config.minMeals,
            minElectricity: config.minElectricity,
            minWater: config.minWater,
            minGas: config.minGas,
            minPlayBudget: config.minPlayBudget,
            prizePocketMoney: config.prizePocketMoney,
            eventConfig: config.eventConfig
        });
        setEditingConfigId(config.id);
        setShowCreateForm(true);
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    function resetForm() {
        setFormData({
            name: '',
            description: '',
            minSavingsRate: 0.2,
            mealCost: 5,
            minMeals: 25,
            minElectricity: 20,
            minWater: 15,
            minGas: 15,
            minPlayBudget: 500,
            prizePocketMoney: 25,
            eventConfig: ''
        });
        setEditingConfigId(null);
        setShowCreateForm(false);
        setFormError('');
    }

    function handleInputChange(e) {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: isNaN(value) ? value : (
                ['minSavingsRate'].includes(name) ? parseFloat(value) : parseInt(value, 10)
            )
        }));
    }

    if (loading) {
        return (
            <div className="budget-game-admin">
                <h1>Budget Game Configuration</h1>
                <p>Loading...</p>
            </div>
        );
    }

    return (
        <div className="budget-game-admin">
            <h1>Budget Game Configuration Admin</h1>
            <p className="subtitle">Manage budget game settings and rules</p>

            {error && <div className="alert alert-error">{error}</div>}
            {successMessage && <div className="alert alert-success">{successMessage}</div>}
            {formError && <div className="alert alert-error">{formError}</div>}

            {/* Create/Edit Form */}
            {showCreateForm && (
                <div className="form-section">
                    <h2>{editingConfigId ? 'Edit Configuration' : 'Create New Configuration'}</h2>
                    <form onSubmit={handleSubmit}>
                        <div className="form-group">
                            <label>Configuration Name</label>
                            <input
                                type="text"
                                name="name"
                                value={formData.name}
                                onChange={handleInputChange}
                                placeholder="e.g., Standard Budget Game"
                                required
                            />
                        </div>

                        <div className="form-group">
                            <label>Description</label>
                            <textarea
                                name="description"
                                value={formData.description}
                                onChange={handleInputChange}
                                placeholder="Describe this configuration"
                                rows="2"
                            />
                        </div>

                        <div className="form-grid">
                            <div className="form-group">
                                <label>Minimum Savings Rate (0.0 - 1.0)</label>
                                <input
                                    type="number"
                                    name="minSavingsRate"
                                    min="0"
                                    max="1"
                                    step="0.01"
                                    value={formData.minSavingsRate}
                                    onChange={handleInputChange}
                                    required
                                />
                                <small>{(formData.minSavingsRate * 100).toFixed(0)}% savings goal</small>
                            </div>

                            <div className="form-group">
                                <label>Meal Cost (€)</label>
                                <input
                                    type="number"
                                    name="mealCost"
                                    min="1"
                                    value={formData.mealCost}
                                    onChange={handleInputChange}
                                    required
                                />
                            </div>

                            <div className="form-group">
                                <label>Minimum Meals</label>
                                <input
                                    type="number"
                                    name="minMeals"
                                    min="1"
                                    value={formData.minMeals}
                                    onChange={handleInputChange}
                                    required
                                />
                                <small>Total food: €{(formData.minMeals * formData.mealCost).toFixed(0)}</small>
                            </div>

                            <div className="form-group">
                                <label>Minimum Electricity (€)</label>
                                <input
                                    type="number"
                                    name="minElectricity"
                                    min="0"
                                    value={formData.minElectricity}
                                    onChange={handleInputChange}
                                    required
                                />
                            </div>

                            <div className="form-group">
                                <label>Minimum Water (€)</label>
                                <input
                                    type="number"
                                    name="minWater"
                                    min="0"
                                    value={formData.minWater}
                                    onChange={handleInputChange}
                                    required
                                />
                            </div>

                            <div className="form-group">
                                <label>Minimum Gas (€)</label>
                                <input
                                    type="number"
                                    name="minGas"
                                    min="0"
                                    value={formData.minGas}
                                    onChange={handleInputChange}
                                    required
                                />
                            </div>

                            <div className="form-group">
                                <label>Minimum Play Budget (€)</label>
                                <input
                                    type="number"
                                    name="minPlayBudget"
                                    min="1"
                                    value={formData.minPlayBudget}
                                    onChange={handleInputChange}
                                    required
                                />
                            </div>

                            <div className="form-group">
                                <label>Prize Pocket Money (€)</label>
                                <input
                                    type="number"
                                    name="prizePocketMoney"
                                    min="0"
                                    value={formData.prizePocketMoney}
                                    onChange={handleInputChange}
                                    required
                                />
                            </div>
                        </div>

                        <div className="form-group">
                            <label>Event Configuration (JSON)</label>
                            <textarea
                                name="eventConfig"
                                value={formData.eventConfig}
                                onChange={handleInputChange}
                                placeholder='{"negative": {...}, "positive": {...}, "neutral": {...}}'
                                rows="8"
                                required
                                className="json-textarea"
                            />
                            <small>
                                Must be valid JSON with negative, positive, and neutral event objects
                            </small>
                        </div>

                        <div className="form-actions">
                            <button type="submit" className="btn btn-primary">
                                {editingConfigId ? 'Update' : 'Create'} Configuration
                            </button>
                            <button type="button" onClick={resetForm} className="btn btn-secondary">
                                Cancel
                            </button>
                        </div>
                    </form>
                </div>
            )}

            {!showCreateForm && (
                <button onClick={() => setShowCreateForm(true)} className="btn btn-primary mb-3">
                    + Create New Configuration
                </button>
            )}

            {/* Configurations List */}
            <div className="configs-section">
                <h2>Existing Configurations</h2>
                {configs.length === 0 ? (
                    <p>No configurations found</p>
                ) : (
                    <div className="configs-grid">
                        {configs.map(config => (
                            <div
                                key={config.id}
                                className={`config-card ${config.isActive ? 'active' : ''}`}
                            >
                                {config.isActive && <div className="active-badge">Active</div>}

                                <h3>{config.name}</h3>
                                {config.description && <p className="description">{config.description}</p>}

                                <div className="config-details">
                                    <div className="detail">
                                        <span className="label">Savings Goal:</span>
                                        <span className="value">{(config.minSavingsRate * 100).toFixed(0)}%</span>
                                    </div>
                                    <div className="detail">
                                        <span className="label">Meal Cost:</span>
                                        <span className="value">€{config.mealCost}</span>
                                    </div>
                                    <div className="detail">
                                        <span className="label">Min Meals:</span>
                                        <span className="value">{config.minMeals}</span>
                                    </div>
                                    <div className="detail">
                                        <span className="label">Min Food Budget:</span>
                                        <span className="value">€{config.minMeals * config.mealCost}</span>
                                    </div>
                                    <div className="detail">
                                        <span className="label">Utilities Min:</span>
                                        <span className="value">€{config.minElectricity + config.minWater + config.minGas}</span>
                                    </div>
                                    <div className="detail">
                                        <span className="label">Min Play Budget:</span>
                                        <span className="value">€{config.minPlayBudget}</span>
                                    </div>
                                    <div className="detail">
                                        <span className="label">Prize:</span>
                                        <span className="value">€{config.prizePocketMoney}</span>
                                    </div>
                                </div>

                                <div className="config-actions">
                                    {!config.isActive && (
                                        <button
                                            onClick={() => handleActivate(config.id)}
                                            className="btn btn-sm btn-success"
                                        >
                                            Activate
                                        </button>
                                    )}
                                    <button
                                        onClick={() => handleEdit(config)}
                                        className="btn btn-sm btn-primary"
                                    >
                                        Edit
                                    </button>
                                    {!config.isActive && (
                                        <button
                                            onClick={() => handleDelete(config.id)}
                                            className="btn btn-sm btn-danger"
                                        >
                                            Delete
                                        </button>
                                    )}
                                </div>

                                <div className="config-meta">
                                    <small>
                                        Created: {new Date(config.createdAt).toLocaleDateString()}
                                    </small>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
}
