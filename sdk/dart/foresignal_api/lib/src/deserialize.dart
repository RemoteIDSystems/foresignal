import 'package:foresignal_api/src/model/absence_window.dart';
import 'package:foresignal_api/src/model/access_grant_input.dart';
import 'package:foresignal_api/src/model/access_request.dart';
import 'package:foresignal_api/src/model/access_request_grant.dart';
import 'package:foresignal_api/src/model/access_request_input.dart';
import 'package:foresignal_api/src/model/access_request_offer_share.dart';
import 'package:foresignal_api/src/model/access_request_page.dart';
import 'package:foresignal_api/src/model/ach_evidence.dart';
import 'package:foresignal_api/src/model/ach_evidence_input.dart';
import 'package:foresignal_api/src/model/ach_matrix.dart';
import 'package:foresignal_api/src/model/ach_matrix_hypotheses_inner.dart';
import 'package:foresignal_api/src/model/action_result.dart';
import 'package:foresignal_api/src/model/active_window.dart';
import 'package:foresignal_api/src/model/active_window_windows_inner.dart';
import 'package:foresignal_api/src/model/admin_token_response.dart';
import 'package:foresignal_api/src/model/aircraft_fusion_payload.dart';
import 'package:foresignal_api/src/model/aircraft_registration_payload.dart';
import 'package:foresignal_api/src/model/ais_gap_payload.dart';
import 'package:foresignal_api/src/model/alert.dart';
import 'package:foresignal_api/src/model/alert_context.dart';
import 'package:foresignal_api/src/model/alert_context_anchor.dart';
import 'package:foresignal_api/src/model/alert_context_near.dart';
import 'package:foresignal_api/src/model/alert_event.dart';
import 'package:foresignal_api/src/model/alert_feedback_input.dart';
import 'package:foresignal_api/src/model/alert_page.dart';
import 'package:foresignal_api/src/model/alert_record_snapshot.dart';
import 'package:foresignal_api/src/model/alert_signal.dart';
import 'package:foresignal_api/src/model/alert_transition_input.dart';
import 'package:foresignal_api/src/model/alertmanager_webhook.dart';
import 'package:foresignal_api/src/model/alertmanager_webhook_alerts_inner.dart';
import 'package:foresignal_api/src/model/allow_external_snapshot200_response.dart';
import 'package:foresignal_api/src/model/allowed_chat.dart';
import 'package:foresignal_api/src/model/analog.dart';
import 'package:foresignal_api/src/model/analog_outcomes.dart';
import 'package:foresignal_api/src/model/analog_outcomes_hypothesis_outcome.dart';
import 'package:foresignal_api/src/model/analog_query.dart';
import 'package:foresignal_api/src/model/analog_result.dart';
import 'package:foresignal_api/src/model/analog_result_outcome_distribution.dart';
import 'package:foresignal_api/src/model/analog_result_signature.dart';
import 'package:foresignal_api/src/model/analog_result_target.dart';
import 'package:foresignal_api/src/model/analysis_callback.dart';
import 'package:foresignal_api/src/model/analysis_cycle.dart';
import 'package:foresignal_api/src/model/analysis_cycle_input.dart';
import 'package:foresignal_api/src/model/analysis_cycle_input_schedule.dart';
import 'package:foresignal_api/src/model/analysis_cycle_page.dart';
import 'package:foresignal_api/src/model/analysis_manifest.dart';
import 'package:foresignal_api/src/model/analysis_manifest_build.dart';
import 'package:foresignal_api/src/model/analysis_manifest_inputs.dart';
import 'package:foresignal_api/src/model/analysis_manifest_model.dart';
import 'package:foresignal_api/src/model/analysis_manifest_model_templates_inner.dart';
import 'package:foresignal_api/src/model/analysis_request_result.dart';
import 'package:foresignal_api/src/model/analysis_request_submission.dart';
import 'package:foresignal_api/src/model/analysis_series.dart';
import 'package:foresignal_api/src/model/analysis_series_input.dart';
import 'package:foresignal_api/src/model/analysis_series_page.dart';
import 'package:foresignal_api/src/model/analyst_claim.dart';
import 'package:foresignal_api/src/model/analyst_cursor.dart';
import 'package:foresignal_api/src/model/analyst_evaluation.dart';
import 'package:foresignal_api/src/model/analyst_evaluation_profiles_inner.dart';
import 'package:foresignal_api/src/model/analyst_evaluation_profiles_inner_tokens_per_day_inner.dart';
import 'package:foresignal_api/src/model/analyst_feedback.dart';
import 'package:foresignal_api/src/model/analyst_feedback_input.dart';
import 'package:foresignal_api/src/model/analyst_note.dart';
import 'package:foresignal_api/src/model/analyst_note_event.dart';
import 'package:foresignal_api/src/model/analyst_note_page.dart';
import 'package:foresignal_api/src/model/analyst_note_patch.dart';
import 'package:foresignal_api/src/model/analyst_profile.dart';
import 'package:foresignal_api/src/model/analyst_profile_input.dart';
import 'package:foresignal_api/src/model/analyst_profile_schedule.dart';
import 'package:foresignal_api/src/model/analyst_profile_scope.dart';
import 'package:foresignal_api/src/model/analyst_proposal.dart';
import 'package:foresignal_api/src/model/analyst_proposal_decision.dart';
import 'package:foresignal_api/src/model/analyst_proposal_event.dart';
import 'package:foresignal_api/src/model/analyst_proposal_page.dart';
import 'package:foresignal_api/src/model/analyst_run.dart';
import 'package:foresignal_api/src/model/analyst_run_input_counts.dart';
import 'package:foresignal_api/src/model/analyst_run_page.dart';
import 'package:foresignal_api/src/model/analytic_finding.dart';
import 'package:foresignal_api/src/model/analytic_finding_dismissal.dart';
import 'package:foresignal_api/src/model/analytic_finding_evidence.dart';
import 'package:foresignal_api/src/model/analytic_finding_inference.dart';
import 'package:foresignal_api/src/model/annotation.dart';
import 'package:foresignal_api/src/model/annotation_input.dart';
import 'package:foresignal_api/src/model/annotation_page.dart';
import 'package:foresignal_api/src/model/annotation_patch.dart';
import 'package:foresignal_api/src/model/annotation_reply_input.dart';
import 'package:foresignal_api/src/model/annotation_resolve_input.dart';
import 'package:foresignal_api/src/model/annotation_thread.dart';
import 'package:foresignal_api/src/model/anomaly_baseline.dart';
import 'package:foresignal_api/src/model/anomaly_baseline_slot.dart';
import 'package:foresignal_api/src/model/anomaly_observation.dart';
import 'package:foresignal_api/src/model/anomaly_observation_baseline.dart';
import 'package:foresignal_api/src/model/anomaly_observation_evidence.dart';
import 'package:foresignal_api/src/model/anomaly_observation_evidence_fetch_log_ranges_inner.dart';
import 'package:foresignal_api/src/model/anomaly_observation_evidence_samples_inner.dart';
import 'package:foresignal_api/src/model/anomaly_observation_page.dart';
import 'package:foresignal_api/src/model/anomaly_record_payload.dart';
import 'package:foresignal_api/src/model/anomaly_window.dart';
import 'package:foresignal_api/src/model/api_token.dart';
import 'package:foresignal_api/src/model/api_token_created.dart';
import 'package:foresignal_api/src/model/api_token_input.dart';
import 'package:foresignal_api/src/model/apns_provider_config.dart';
import 'package:foresignal_api/src/model/approach_condition.dart';
import 'package:foresignal_api/src/model/approach_condition_target.dart';
import 'package:foresignal_api/src/model/archive_partition.dart';
import 'package:foresignal_api/src/model/archive_partition_files_inner.dart';
import 'package:foresignal_api/src/model/archive_partition_page.dart';
import 'package:foresignal_api/src/model/archive_settings.dart';
import 'package:foresignal_api/src/model/archive_settings_s3.dart';
import 'package:foresignal_api/src/model/archive_settings_secrets.dart';
import 'package:foresignal_api/src/model/ask_action.dart';
import 'package:foresignal_api/src/model/ask_citation.dart';
import 'package:foresignal_api/src/model/ask_delta_event.dart';
import 'package:foresignal_api/src/model/ask_message.dart';
import 'package:foresignal_api/src/model/ask_progress_event.dart';
import 'package:foresignal_api/src/model/ask_request.dart';
import 'package:foresignal_api/src/model/ask_response.dart';
import 'package:foresignal_api/src/model/ask_response_usage.dart';
import 'package:foresignal_api/src/model/ask_scope.dart';
import 'package:foresignal_api/src/model/ask_stream200_response.dart';
import 'package:foresignal_api/src/model/ask_thread.dart';
import 'package:foresignal_api/src/model/ask_thread_page.dart';
import 'package:foresignal_api/src/model/ask_thread_page_items_inner.dart';
import 'package:foresignal_api/src/model/ask_warning_event.dart';
import 'package:foresignal_api/src/model/assessment.dart';
import 'package:foresignal_api/src/model/assessment_bundle.dart';
import 'package:foresignal_api/src/model/assessment_bundle_objects_inner.dart';
import 'package:foresignal_api/src/model/assessment_coverage.dart';
import 'package:foresignal_api/src/model/assessment_coverage_situations_inner.dart';
import 'package:foresignal_api/src/model/assessment_feedback.dart';
import 'package:foresignal_api/src/model/assessment_feedback_input.dart';
import 'package:foresignal_api/src/model/assessment_page.dart';
import 'package:foresignal_api/src/model/assessment_prompt_templates_inner.dart';
import 'package:foresignal_api/src/model/assessment_published_event.dart';
import 'package:foresignal_api/src/model/assessment_sections_inner.dart';
import 'package:foresignal_api/src/model/attachment.dart';
import 'package:foresignal_api/src/model/attention_gap_event.dart';
import 'package:foresignal_api/src/model/attention_topic.dart';
import 'package:foresignal_api/src/model/attention_topic_input.dart';
import 'package:foresignal_api/src/model/attention_topic_input_wiki_articles_inner.dart';
import 'package:foresignal_api/src/model/attention_topic_page.dart';
import 'package:foresignal_api/src/model/attention_topic_social.dart';
import 'package:foresignal_api/src/model/attention_topic_social_accounts_inner.dart';
import 'package:foresignal_api/src/model/attention_topic_social_channel.dart';
import 'package:foresignal_api/src/model/attribution.dart';
import 'package:foresignal_api/src/model/attribution_summary.dart';
import 'package:foresignal_api/src/model/audit_event.dart';
import 'package:foresignal_api/src/model/audit_event_page.dart';
import 'package:foresignal_api/src/model/auto_resolve.dart';
import 'package:foresignal_api/src/model/backfill_job.dart';
import 'package:foresignal_api/src/model/backtest_caveat.dart';
import 'package:foresignal_api/src/model/backtest_job.dart';
import 'package:foresignal_api/src/model/backtest_job_page.dart';
import 'package:foresignal_api/src/model/backtest_job_result.dart';
import 'package:foresignal_api/src/model/backtest_request.dart';
import 'package:foresignal_api/src/model/base_rate.dart';
import 'package:foresignal_api/src/model/base_rate_definition.dart';
import 'package:foresignal_api/src/model/base_rate_definition_condition.dart';
import 'package:foresignal_api/src/model/base_rate_input.dart';
import 'package:foresignal_api/src/model/base_rate_licences_inner.dart';
import 'package:foresignal_api/src/model/base_rate_page.dart';
import 'package:foresignal_api/src/model/base_rate_refresh_result.dart';
import 'package:foresignal_api/src/model/base_rate_unconditional.dart';
import 'package:foresignal_api/src/model/baseline_rebuild_result.dart';
import 'package:foresignal_api/src/model/benchmark_case.dart';
import 'package:foresignal_api/src/model/benchmark_case_expectations.dart';
import 'package:foresignal_api/src/model/benchmark_case_expectations_findings_inner.dart';
import 'package:foresignal_api/src/model/benchmark_case_expectations_precursors_inner.dart';
import 'package:foresignal_api/src/model/benchmark_case_expectations_questions_inner.dart';
import 'package:foresignal_api/src/model/benchmark_case_input.dart';
import 'package:foresignal_api/src/model/benchmark_case_page.dart';
import 'package:foresignal_api/src/model/benchmark_component_result.dart';
import 'package:foresignal_api/src/model/benchmark_component_result_scenarios_inner.dart';
import 'package:foresignal_api/src/model/benchmark_component_result_thresholds_inner.dart';
import 'package:foresignal_api/src/model/benchmark_run.dart';
import 'package:foresignal_api/src/model/benchmark_run_input.dart';
import 'package:foresignal_api/src/model/benchmark_run_page.dart';
import 'package:foresignal_api/src/model/benchmark_status.dart';
import 'package:foresignal_api/src/model/benchmark_status_components_inner.dart';
import 'package:foresignal_api/src/model/benchmark_status_components_inner_gating.dart';
import 'package:foresignal_api/src/model/briefing.dart';
import 'package:foresignal_api/src/model/briefing_input.dart';
import 'package:foresignal_api/src/model/briefing_input_audio.dart';
import 'package:foresignal_api/src/model/briefing_run.dart';
import 'package:foresignal_api/src/model/briefing_run_audio.dart';
import 'package:foresignal_api/src/model/briefing_run_page.dart';
import 'package:foresignal_api/src/model/briefing_scope.dart';
import 'package:foresignal_api/src/model/budget.dart';
import 'package:foresignal_api/src/model/cable_proximity_risk_payload.dart';
import 'package:foresignal_api/src/model/canonical_event.dart';
import 'package:foresignal_api/src/model/canonical_event_page.dart';
import 'package:foresignal_api/src/model/capture_mode.dart';
import 'package:foresignal_api/src/model/captured_sample.dart';
import 'package:foresignal_api/src/model/caveat.dart';
import 'package:foresignal_api/src/model/celestrak_gp_element.dart';
import 'package:foresignal_api/src/model/change.dart';
import 'package:foresignal_api/src/model/change_event.dart';
import 'package:foresignal_api/src/model/change_page.dart';
import 'package:foresignal_api/src/model/channel.dart';
import 'package:foresignal_api/src/model/channel_actions_config.dart';
import 'package:foresignal_api/src/model/channel_airtime.dart';
import 'package:foresignal_api/src/model/channel_input.dart';
import 'package:foresignal_api/src/model/channel_input_config.dart';
import 'package:foresignal_api/src/model/channel_interactive_status.dart';
import 'package:foresignal_api/src/model/channel_interactive_status_chats_inner.dart';
import 'package:foresignal_api/src/model/channel_test_result.dart';
import 'package:foresignal_api/src/model/channel_type.dart';
import 'package:foresignal_api/src/model/channel_type_capabilities.dart';
import 'package:foresignal_api/src/model/channel_type_config_fields_inner.dart';
import 'package:foresignal_api/src/model/channel_warnings_inner.dart';
import 'package:foresignal_api/src/model/chokepoint_activity_payload.dart';
import 'package:foresignal_api/src/model/chokepoint_uturn_payload.dart';
import 'package:foresignal_api/src/model/chronicle.dart';
import 'package:foresignal_api/src/model/chronicle_access_event.dart';
import 'package:foresignal_api/src/model/chronicle_activate_request.dart';
import 'package:foresignal_api/src/model/chronicle_activate_request_backfill.dart';
import 'package:foresignal_api/src/model/chronicle_archive.dart';
import 'package:foresignal_api/src/model/chronicle_area.dart';
import 'package:foresignal_api/src/model/chronicle_cluster.dart';
import 'package:foresignal_api/src/model/chronicle_cluster_members_inner.dart';
import 'package:foresignal_api/src/model/chronicle_cluster_page.dart';
import 'package:foresignal_api/src/model/chronicle_estimate.dart';
import 'package:foresignal_api/src/model/chronicle_estimate_layers_inner.dart';
import 'package:foresignal_api/src/model/chronicle_export_request.dart';
import 'package:foresignal_api/src/model/chronicle_external_stream_selector.dart';
import 'package:foresignal_api/src/model/chronicle_frame_index.dart';
import 'package:foresignal_api/src/model/chronicle_frame_index_frames_inner.dart';
import 'package:foresignal_api/src/model/chronicle_input.dart';
import 'package:foresignal_api/src/model/chronicle_layer.dart';
import 'package:foresignal_api/src/model/chronicle_layer_estimate.dart';
import 'package:foresignal_api/src/model/chronicle_layer_input.dart';
import 'package:foresignal_api/src/model/chronicle_layer_patch.dart';
import 'package:foresignal_api/src/model/chronicle_layer_selector.dart';
import 'package:foresignal_api/src/model/chronicle_layer_selector_one_of.dart';
import 'package:foresignal_api/src/model/chronicle_layer_selector_one_of1.dart';
import 'package:foresignal_api/src/model/chronicle_layer_selector_one_of2.dart';
import 'package:foresignal_api/src/model/chronicle_layer_selector_one_of3.dart';
import 'package:foresignal_api/src/model/chronicle_layer_selector_one_of4.dart';
import 'package:foresignal_api/src/model/chronicle_layer_selector_one_of5.dart';
import 'package:foresignal_api/src/model/chronicle_layer_selector_one_of6.dart';
import 'package:foresignal_api/src/model/chronicle_layer_selector_one_of7.dart';
import 'package:foresignal_api/src/model/chronicle_layer_selector_one_of8.dart';
import 'package:foresignal_api/src/model/chronicle_layer_stats.dart';
import 'package:foresignal_api/src/model/chronicle_link.dart';
import 'package:foresignal_api/src/model/chronicle_link_found_event.dart';
import 'package:foresignal_api/src/model/chronicle_link_scores.dart';
import 'package:foresignal_api/src/model/chronicle_link_scores_evidence.dart';
import 'package:foresignal_api/src/model/chronicle_page.dart';
import 'package:foresignal_api/src/model/chronicle_patch.dart';
import 'package:foresignal_api/src/model/chronicle_record_version.dart';
import 'package:foresignal_api/src/model/chronicle_related_page.dart';
import 'package:foresignal_api/src/model/chronicle_related_page_items_inner.dart';
import 'package:foresignal_api/src/model/chronicle_series.dart';
import 'package:foresignal_api/src/model/chronicle_sketch_batch.dart';
import 'package:foresignal_api/src/model/chronicle_sketch_batch_access_decisions_inner.dart';
import 'package:foresignal_api/src/model/chronicle_sketch_batch_access_requests_inner.dart';
import 'package:foresignal_api/src/model/chronicle_sketch_batch_result.dart';
import 'package:foresignal_api/src/model/chronicle_sketch_batch_result_rejected_inner.dart';
import 'package:foresignal_api/src/model/chronicle_sketch_package.dart';
import 'package:foresignal_api/src/model/chronicle_sketch_package_capture_span.dart';
import 'package:foresignal_api/src/model/chronicle_social_narratives_selector.dart';
import 'package:foresignal_api/src/model/chronicle_social_volume_selector.dart';
import 'package:foresignal_api/src/model/chronicle_social_volume_selector_scopes_inner.dart';
import 'package:foresignal_api/src/model/chronicle_state.dart';
import 'package:foresignal_api/src/model/chronicle_state_layers_inner.dart';
import 'package:foresignal_api/src/model/chronicle_state_layers_inner_rollups_inner.dart';
import 'package:foresignal_api/src/model/chronicle_status_event.dart';
import 'package:foresignal_api/src/model/chronicle_suggest_request.dart';
import 'package:foresignal_api/src/model/chronicle_suggestion.dart';
import 'package:foresignal_api/src/model/chronicle_suggestion_estimate.dart';
import 'package:foresignal_api/src/model/chronicle_suggestion_estimate_layers_inner.dart';
import 'package:foresignal_api/src/model/chronicle_summary.dart';
import 'package:foresignal_api/src/model/chronicle_summary_gaps_inner.dart';
import 'package:foresignal_api/src/model/chronicle_summary_layers_inner.dart';
import 'package:foresignal_api/src/model/chronicle_summary_layers_inner_series_inner.dart';
import 'package:foresignal_api/src/model/chronicle_summary_storage.dart';
import 'package:foresignal_api/src/model/chronicle_summary_top_entities_inner.dart';
import 'package:foresignal_api/src/model/chronicle_thread_item.dart';
import 'package:foresignal_api/src/model/chronicle_thread_item_gap.dart';
import 'package:foresignal_api/src/model/chronicle_thread_page.dart';
import 'package:foresignal_api/src/model/chronicle_thread_read_request.dart';
import 'package:foresignal_api/src/model/claim.dart';
import 'package:foresignal_api/src/model/claim_event_time.dart';
import 'package:foresignal_api/src/model/claim_extracted_event.dart';
import 'package:foresignal_api/src/model/claim_page.dart';
import 'package:foresignal_api/src/model/claim_place.dart';
import 'package:foresignal_api/src/model/claim_reputation.dart';
import 'package:foresignal_api/src/model/client_links.dart';
import 'package:foresignal_api/src/model/client_presence.dart';
import 'package:foresignal_api/src/model/client_presence_input.dart';
import 'package:foresignal_api/src/model/cloud_event.dart';
import 'package:foresignal_api/src/model/cluster_timeline_item.dart';
import 'package:foresignal_api/src/model/cluster_timeline_page.dart';
import 'package:foresignal_api/src/model/collab_attachment.dart';
import 'package:foresignal_api/src/model/collab_attachment_image_meta.dart';
import 'package:foresignal_api/src/model/collab_attachment_links.dart';
import 'package:foresignal_api/src/model/collection_gap.dart';
import 'package:foresignal_api/src/model/collection_gap_page.dart';
import 'package:foresignal_api/src/model/collection_gap_recommendations_inner.dart';
import 'package:foresignal_api/src/model/collector_settings.dart';
import 'package:foresignal_api/src/model/collector_status.dart';
import 'package:foresignal_api/src/model/comment_event.dart';
import 'package:foresignal_api/src/model/common_dependencies.dart';
import 'package:foresignal_api/src/model/common_dependencies_dependencies_inner.dart';
import 'package:foresignal_api/src/model/common_dependencies_dependencies_inner_paths_inner.dart';
import 'package:foresignal_api/src/model/common_dependencies_request.dart';
import 'package:foresignal_api/src/model/compact_message.dart';
import 'package:foresignal_api/src/model/condition.dart';
import 'package:foresignal_api/src/model/context_link.dart';
import 'package:foresignal_api/src/model/context_link_request.dart';
import 'package:foresignal_api/src/model/context_patch_event.dart';
import 'package:foresignal_api/src/model/context_summary.dart';
import 'package:foresignal_api/src/model/context_summary_activity_inner.dart';
import 'package:foresignal_api/src/model/context_summary_insights_inner.dart';
import 'package:foresignal_api/src/model/context_summary_layer_counts_inner.dart';
import 'package:foresignal_api/src/model/context_summary_narrative.dart';
import 'package:foresignal_api/src/model/context_summary_request.dart';
import 'package:foresignal_api/src/model/contract.dart';
import 'package:foresignal_api/src/model/coordination_detected_event.dart';
import 'package:foresignal_api/src/model/coordination_indicator.dart';
import 'package:foresignal_api/src/model/coordination_indicators.dart';
import 'package:foresignal_api/src/model/correlation_recipe.dart';
import 'package:foresignal_api/src/model/correlation_recipe_create.dart';
import 'package:foresignal_api/src/model/correlation_recipe_definition.dart';
import 'package:foresignal_api/src/model/correlation_recipe_definition_activate_at.dart';
import 'package:foresignal_api/src/model/correlation_recipe_definition_contradictions_inner.dart';
import 'package:foresignal_api/src/model/correlation_recipe_definition_dedup.dart';
import 'package:foresignal_api/src/model/correlation_recipe_definition_joins_inner.dart';
import 'package:foresignal_api/src/model/correlation_recipe_definition_joins_inner_window.dart';
import 'package:foresignal_api/src/model/correlation_recipe_definition_seed.dart';
import 'package:foresignal_api/src/model/correlation_recipe_update.dart';
import 'package:foresignal_api/src/model/corroboration_payload.dart';
import 'package:foresignal_api/src/model/cors_settings.dart';
import 'package:foresignal_api/src/model/cot_backfill_request.dart';
import 'package:foresignal_api/src/model/count_result.dart';
import 'package:foresignal_api/src/model/count_window.dart';
import 'package:foresignal_api/src/model/country_ref.dart';
import 'package:foresignal_api/src/model/coverage.dart';
import 'package:foresignal_api/src/model/coverage_json.dart';
import 'package:foresignal_api/src/model/coverage_stats.dart';
import 'package:foresignal_api/src/model/coverage_stats_articles.dart';
import 'package:foresignal_api/src/model/coverage_stats_coverage_balance.dart';
import 'package:foresignal_api/src/model/cpa_batch_request.dart';
import 'package:foresignal_api/src/model/cpa_batch_request_filter.dart';
import 'package:foresignal_api/src/model/cpa_batch_result.dart';
import 'package:foresignal_api/src/model/cpa_payload.dart';
import 'package:foresignal_api/src/model/cpa_result.dart';
import 'package:foresignal_api/src/model/credential_field.dart';
import 'package:foresignal_api/src/model/credentials_update.dart';
import 'package:foresignal_api/src/model/custom_source.dart';
import 'package:foresignal_api/src/model/custom_source_created.dart';
import 'package:foresignal_api/src/model/custom_source_formats.dart';
import 'package:foresignal_api/src/model/custom_source_formats_alertmanager.dart';
import 'package:foresignal_api/src/model/custom_source_formats_cap.dart';
import 'package:foresignal_api/src/model/custom_source_formats_cloudevents.dart';
import 'package:foresignal_api/src/model/custom_source_formats_gpslogger.dart';
import 'package:foresignal_api/src/model/custom_source_formats_osmand.dart';
import 'package:foresignal_api/src/model/custom_source_formats_overland.dart';
import 'package:foresignal_api/src/model/custom_source_formats_owntracks.dart';
import 'package:foresignal_api/src/model/custom_source_input.dart';
import 'package:foresignal_api/src/model/cycle_packet.dart';
import 'package:foresignal_api/src/model/cycle_packet_coverage.dart';
import 'package:foresignal_api/src/model/cycle_packet_forecasts.dart';
import 'package:foresignal_api/src/model/cycle_packet_hypotheses.dart';
import 'package:foresignal_api/src/model/cycle_packet_item.dart';
import 'package:foresignal_api/src/model/cycle_packet_outlooks.dart';
import 'package:foresignal_api/src/model/cycle_packet_situations.dart';
import 'package:foresignal_api/src/model/cycle_run.dart';
import 'package:foresignal_api/src/model/cycle_run_failed_event.dart';
import 'package:foresignal_api/src/model/cycle_run_page.dart';
import 'package:foresignal_api/src/model/cycle_run_roles_inner.dart';
import 'package:foresignal_api/src/model/cycle_run_roles_inner_prompt_template.dart';
import 'package:foresignal_api/src/model/cycle_scope.dart';
import 'package:foresignal_api/src/model/dark_vessel_candidate_payload.dart';
import 'package:foresignal_api/src/model/dcp_observation_payload.dart';
import 'package:foresignal_api/src/model/deception_payload.dart';
import 'package:foresignal_api/src/model/deception_payload_all_of_checks.dart';
import 'package:foresignal_api/src/model/declarative_push_payload.dart';
import 'package:foresignal_api/src/model/declarative_push_payload_notification.dart';
import 'package:foresignal_api/src/model/declared_observability.dart';
import 'package:foresignal_api/src/model/declared_observability_kinds_value.dart';
import 'package:foresignal_api/src/model/declared_observability_kinds_value_area.dart';
import 'package:foresignal_api/src/model/declared_observability_kinds_value_thresholds_inner.dart';
import 'package:foresignal_api/src/model/derived_evidence_ref.dart';
import 'package:foresignal_api/src/model/derived_payload_common.dart';
import 'package:foresignal_api/src/model/derived_payload_common_detector.dart';
import 'package:foresignal_api/src/model/detectability.dart';
import 'package:foresignal_api/src/model/detector_ref.dart';
import 'package:foresignal_api/src/model/device_info.dart';
import 'package:foresignal_api/src/model/digest.dart';
import 'package:foresignal_api/src/model/digest_active.dart';
import 'package:foresignal_api/src/model/digest_alerts.dart';
import 'package:foresignal_api/src/model/digest_alerts_by_rule_inner.dart';
import 'package:foresignal_api/src/model/digest_alerts_by_severity.dart';
import 'package:foresignal_api/src/model/digest_alerts_by_status.dart';
import 'package:foresignal_api/src/model/digest_all_of_next.dart';
import 'package:foresignal_api/src/model/digest_count.dart';
import 'package:foresignal_api/src/model/digest_count_entities.dart';
import 'package:foresignal_api/src/model/digest_count_previous.dart';
import 'package:foresignal_api/src/model/digest_health_item.dart';
import 'package:foresignal_api/src/model/digest_notable_item.dart';
import 'package:foresignal_api/src/model/digest_stats.dart';
import 'package:foresignal_api/src/model/digest_stats_notable_inner.dart';
import 'package:foresignal_api/src/model/discoverability.dart';
import 'package:foresignal_api/src/model/doctor_finding.dart';
import 'package:foresignal_api/src/model/doctor_options.dart';
import 'package:foresignal_api/src/model/doctor_report.dart';
import 'package:foresignal_api/src/model/doctor_report_fixture.dart';
import 'package:foresignal_api/src/model/doctor_report_llm.dart';
import 'package:foresignal_api/src/model/doctor_report_llm_tokens.dart';
import 'package:foresignal_api/src/model/doctor_report_page.dart';
import 'package:foresignal_api/src/model/doctor_report_probe.dart';
import 'package:foresignal_api/src/model/doctor_report_summary.dart';
import 'package:foresignal_api/src/model/doctor_report_summary_top_finding.dart';
import 'package:foresignal_api/src/model/doctor_run_accepted.dart';
import 'package:foresignal_api/src/model/dossier.dart';
import 'package:foresignal_api/src/model/dossier_coverage.dart';
import 'package:foresignal_api/src/model/dossier_sections.dart';
import 'package:foresignal_api/src/model/dossier_sections_analysis.dart';
import 'package:foresignal_api/src/model/dossier_sections_forecast.dart';
import 'package:foresignal_api/src/model/dossier_sections_forecast_official_inner.dart';
import 'package:foresignal_api/src/model/dossier_sections_imagery.dart';
import 'package:foresignal_api/src/model/dossier_sections_imagery_items_inner.dart';
import 'package:foresignal_api/src/model/dossier_sections_impacts.dart';
import 'package:foresignal_api/src/model/dossier_sections_impacts_items_inner.dart';
import 'package:foresignal_api/src/model/dossier_sections_overview.dart';
import 'package:foresignal_api/src/model/dossier_sections_overview_facets.dart';
import 'package:foresignal_api/src/model/dossier_sections_overview_links.dart';
import 'package:foresignal_api/src/model/dossier_sections_related.dart';
import 'package:foresignal_api/src/model/dossier_sections_timeline.dart';
import 'package:foresignal_api/src/model/dossier_sections_timeline_items_inner.dart';
import 'package:foresignal_api/src/model/edp_analysis_request.dart';
import 'package:foresignal_api/src/model/edp_batch.dart';
import 'package:foresignal_api/src/model/edp_coverage.dart';
import 'package:foresignal_api/src/model/edp_delete_item.dart';
import 'package:foresignal_api/src/model/edp_document_item.dart';
import 'package:foresignal_api/src/model/edp_document_item_author.dart';
import 'package:foresignal_api/src/model/edp_document_item_media_inner.dart';
import 'package:foresignal_api/src/model/edp_event_item.dart';
import 'package:foresignal_api/src/model/edp_feature_item.dart';
import 'package:foresignal_api/src/model/edp_forecast_item.dart';
import 'package:foresignal_api/src/model/edp_forecast_item_all_of_question.dart';
import 'package:foresignal_api/src/model/edp_geometry.dart';
import 'package:foresignal_api/src/model/edp_ingest_result.dart';
import 'package:foresignal_api/src/model/edp_item.dart';
import 'package:foresignal_api/src/model/edp_item_rejection.dart';
import 'package:foresignal_api/src/model/edp_item_rejection_schema_errors_inner.dart';
import 'package:foresignal_api/src/model/edp_manifest.dart';
import 'package:foresignal_api/src/model/edp_observation_item.dart';
import 'package:foresignal_api/src/model/edp_origin.dart';
import 'package:foresignal_api/src/model/edp_presentation.dart';
import 'package:foresignal_api/src/model/edp_protocol_info.dart';
import 'package:foresignal_api/src/model/edp_pull.dart';
import 'package:foresignal_api/src/model/edp_search.dart';
import 'package:foresignal_api/src/model/edp_snapshot.dart';
import 'package:foresignal_api/src/model/edp_source_info.dart';
import 'package:foresignal_api/src/model/edp_source_info_access.dart';
import 'package:foresignal_api/src/model/edp_source_info_licence.dart';
import 'package:foresignal_api/src/model/edp_source_info_originator.dart';
import 'package:foresignal_api/src/model/edp_source_info_publisher.dart';
import 'package:foresignal_api/src/model/edp_source_info_terms.dart';
import 'package:foresignal_api/src/model/edp_status_item.dart';
import 'package:foresignal_api/src/model/edp_stream.dart';
import 'package:foresignal_api/src/model/edp_stream_fields.dart';
import 'package:foresignal_api/src/model/edp_suggested_binding.dart';
import 'package:foresignal_api/src/model/edp_transport.dart';
import 'package:foresignal_api/src/model/edp_validation_report.dart';
import 'package:foresignal_api/src/model/edp_validation_request.dart';
import 'package:foresignal_api/src/model/edr_instance_list.dart';
import 'package:foresignal_api/src/model/edr_instance_list_instances_inner.dart';
import 'package:foresignal_api/src/model/emergency_squawk_payload.dart';
import 'package:foresignal_api/src/model/emwin_product_payload.dart';
import 'package:foresignal_api/src/model/emwin_warning_payload.dart';
import 'package:foresignal_api/src/model/emwin_warning_payload_duplicate_of.dart';
import 'package:foresignal_api/src/model/enable_core_sources_result.dart';
import 'package:foresignal_api/src/model/enable_free_sources_result.dart';
import 'package:foresignal_api/src/model/ensemble_refit_result.dart';
import 'package:foresignal_api/src/model/ensemble_refit_result_extremizing.dart';
import 'package:foresignal_api/src/model/ensemble_refit_result_platt_inner.dart';
import 'package:foresignal_api/src/model/ensemble_refit_result_weights_inner.dart';
import 'package:foresignal_api/src/model/entity.dart';
import 'package:foresignal_api/src/model/entity_alias.dart';
import 'package:foresignal_api/src/model/entity_key.dart';
import 'package:foresignal_api/src/model/entity_key_input.dart';
import 'package:foresignal_api/src/model/entity_link.dart';
import 'package:foresignal_api/src/model/entity_merge_input.dart';
import 'package:foresignal_api/src/model/entity_page.dart';
import 'package:foresignal_api/src/model/entity_record.dart';
import 'package:foresignal_api/src/model/entity_record_list.dart';
import 'package:foresignal_api/src/model/escalation.dart';
import 'package:foresignal_api/src/model/escalation_policy.dart';
import 'package:foresignal_api/src/model/escalation_policy_input.dart';
import 'package:foresignal_api/src/model/escalation_policy_input_repeat.dart';
import 'package:foresignal_api/src/model/escalation_policy_input_stop_on.dart';
import 'package:foresignal_api/src/model/escalation_step.dart';
import 'package:foresignal_api/src/model/eta_result.dart';
import 'package:foresignal_api/src/model/eta_result_target.dart';
import 'package:foresignal_api/src/model/event_selector.dart';
import 'package:foresignal_api/src/model/event_selector_change_point.dart';
import 'package:foresignal_api/src/model/event_selector_situation_activated.dart';
import 'package:foresignal_api/src/model/event_selector_unexplained_move.dart';
import 'package:foresignal_api/src/model/event_stream_alerts_filter.dart';
import 'package:foresignal_api/src/model/event_stream_changes_filter.dart';
import 'package:foresignal_api/src/model/event_stream_filter.dart';
import 'package:foresignal_api/src/model/event_stream_filter_alerts.dart';
import 'package:foresignal_api/src/model/event_stream_filter_chronicle.dart';
import 'package:foresignal_api/src/model/event_stream_filter_external.dart';
import 'package:foresignal_api/src/model/event_stream_filter_workspace.dart';
import 'package:foresignal_api/src/model/event_study_request.dart';
import 'package:foresignal_api/src/model/event_study_result.dart';
import 'package:foresignal_api/src/model/event_study_result_caar.dart';
import 'package:foresignal_api/src/model/event_study_result_events_inner.dart';
import 'package:foresignal_api/src/model/event_study_result_events_inner_abnormal_inner.dart';
import 'package:foresignal_api/src/model/event_study_result_events_inner_car.dart';
import 'package:foresignal_api/src/model/event_study_result_windows.dart';
import 'package:foresignal_api/src/model/event_study_window_stats.dart';
import 'package:foresignal_api/src/model/event_study_window_stats_placebo.dart';
import 'package:foresignal_api/src/model/evidence_event.dart';
import 'package:foresignal_api/src/model/evidence_group.dart';
import 'package:foresignal_api/src/model/evidence_item.dart';
import 'package:foresignal_api/src/model/evidence_item_input.dart';
import 'package:foresignal_api/src/model/evidence_item_page.dart';
import 'package:foresignal_api/src/model/evidence_item_patch.dart';
import 'package:foresignal_api/src/model/exchange_package.dart';
import 'package:foresignal_api/src/model/exchange_package_deliveries_inner.dart';
import 'package:foresignal_api/src/model/exchange_package_input.dart';
import 'package:foresignal_api/src/model/exchange_package_input_extract.dart';
import 'package:foresignal_api/src/model/exchange_package_page.dart';
import 'package:foresignal_api/src/model/exchange_receipt.dart';
import 'package:foresignal_api/src/model/exercise_run.dart';
import 'package:foresignal_api/src/model/exercise_run_create.dart';
import 'package:foresignal_api/src/model/exercise_run_page.dart';
import 'package:foresignal_api/src/model/exercise_stats.dart';
import 'package:foresignal_api/src/model/exercise_stats_skipped_rules_inner.dart';
import 'package:foresignal_api/src/model/export_file.dart';
import 'package:foresignal_api/src/model/export_job.dart';
import 'package:foresignal_api/src/model/export_job_page.dart';
import 'package:foresignal_api/src/model/export_licence_summary.dart';
import 'package:foresignal_api/src/model/export_licence_summary_excluded_inner.dart';
import 'package:foresignal_api/src/model/export_licence_summary_licences_inner.dart';
import 'package:foresignal_api/src/model/export_manifest.dart';
import 'package:foresignal_api/src/model/export_manifest_files_inner.dart';
import 'package:foresignal_api/src/model/export_manifest_signature.dart';
import 'package:foresignal_api/src/model/export_spec.dart';
import 'package:foresignal_api/src/model/exposure.dart';
import 'package:foresignal_api/src/model/exposure_area.dart';
import 'package:foresignal_api/src/model/exposure_dependents.dart';
import 'package:foresignal_api/src/model/exposure_dependents_by_label.dart';
import 'package:foresignal_api/src/model/exposure_facilities.dart';
import 'package:foresignal_api/src/model/exposure_places.dart';
import 'package:foresignal_api/src/model/exposure_places_items_inner.dart';
import 'package:foresignal_api/src/model/exposure_population.dart';
import 'package:foresignal_api/src/model/exposure_request.dart';
import 'package:foresignal_api/src/model/exposure_traffic.dart';
import 'package:foresignal_api/src/model/exposure_watchlist_entities.dart';
import 'package:foresignal_api/src/model/exposure_watchlist_entities_counts_inner.dart';
import 'package:foresignal_api/src/model/external_binding.dart';
import 'package:foresignal_api/src/model/external_binding_event.dart';
import 'package:foresignal_api/src/model/external_binding_input.dart';
import 'package:foresignal_api/src/model/external_binding_preview.dart';
import 'package:foresignal_api/src/model/external_binding_preview_effects_inner.dart';
import 'package:foresignal_api/src/model/external_catalogue_entry.dart';
import 'package:foresignal_api/src/model/external_catalogue_entry_time_dimension.dart';
import 'package:foresignal_api/src/model/external_forecaster_registration.dart';
import 'package:foresignal_api/src/model/external_question_link.dart';
import 'package:foresignal_api/src/model/external_question_link_input.dart';
import 'package:foresignal_api/src/model/external_source.dart';
import 'package:foresignal_api/src/model/external_source_created.dart';
import 'package:foresignal_api/src/model/external_source_page.dart';
import 'package:foresignal_api/src/model/external_source_pull.dart';
import 'package:foresignal_api/src/model/external_source_registration.dart';
import 'package:foresignal_api/src/model/external_source_state_event.dart';
import 'package:foresignal_api/src/model/external_stream_stats.dart';
import 'package:foresignal_api/src/model/fcm_provider_config.dart';
import 'package:foresignal_api/src/model/feature.dart';
import 'package:foresignal_api/src/model/feature_collection.dart';
import 'package:foresignal_api/src/model/feature_properties.dart';
import 'package:foresignal_api/src/model/federation_change_page.dart';
import 'package:foresignal_api/src/model/federation_info.dart';
import 'package:foresignal_api/src/model/federation_ingest_batch.dart';
import 'package:foresignal_api/src/model/federation_ingest_result.dart';
import 'package:foresignal_api/src/model/federation_ingest_result_dropped.dart';
import 'package:foresignal_api/src/model/federation_item.dart';
import 'package:foresignal_api/src/model/federation_snapshot_page.dart';
import 'package:foresignal_api/src/model/feed_key.dart';
import 'package:foresignal_api/src/model/feed_key_created.dart';
import 'package:foresignal_api/src/model/feed_key_created_all_of_urls.dart';
import 'package:foresignal_api/src/model/feed_key_input.dart';
import 'package:foresignal_api/src/model/felt_report_estimate_payload.dart';
import 'package:foresignal_api/src/model/fetch_log_entry.dart';
import 'package:foresignal_api/src/model/fetch_outcome.dart';
import 'package:foresignal_api/src/model/fetch_page.dart';
import 'package:foresignal_api/src/model/field_encodings.dart';
import 'package:foresignal_api/src/model/field_encodings_i16.dart';
import 'package:foresignal_api/src/model/field_encodings_png_tiles.dart';
import 'package:foresignal_api/src/model/field_encodings_png_uv.dart';
import 'package:foresignal_api/src/model/field_encodings_velocity_json.dart';
import 'package:foresignal_api/src/model/field_manifest.dart';
import 'package:foresignal_api/src/model/field_manifest_components_inner.dart';
import 'package:foresignal_api/src/model/field_manifest_cycle.dart';
import 'package:foresignal_api/src/model/field_manifest_grid.dart';
import 'package:foresignal_api/src/model/field_spec.dart';
import 'package:foresignal_api/src/model/field_threshold.dart';
import 'package:foresignal_api/src/model/filter_applied_event.dart';
import 'package:foresignal_api/src/model/finding_base_rate.dart';
import 'package:foresignal_api/src/model/finding_event.dart';
import 'package:foresignal_api/src/model/finding_event_finding.dart';
import 'package:foresignal_api/src/model/finding_page.dart';
import 'package:foresignal_api/src/model/finding_patch.dart';
import 'package:foresignal_api/src/model/fire_cluster_payload.dart';
import 'package:foresignal_api/src/model/fire_detection_payload.dart';
import 'package:foresignal_api/src/model/forecast.dart';
import 'package:foresignal_api/src/model/forecast_annul_input.dart';
import 'package:foresignal_api/src/model/forecast_area_payload.dart';
import 'package:foresignal_api/src/model/forecast_area_payload_threshold.dart';
import 'package:foresignal_api/src/model/forecast_calibration.dart';
import 'package:foresignal_api/src/model/forecast_calibration_bins_inner.dart';
import 'package:foresignal_api/src/model/forecast_calibration_curve_inner.dart';
import 'package:foresignal_api/src/model/forecast_calibration_decomposition.dart';
import 'package:foresignal_api/src/model/forecast_citations_inner.dart';
import 'package:foresignal_api/src/model/forecast_evaluation.dart';
import 'package:foresignal_api/src/model/forecast_evaluation_request.dart';
import 'package:foresignal_api/src/model/forecast_evaluation_skipped.dart';
import 'package:foresignal_api/src/model/forecast_history.dart';
import 'package:foresignal_api/src/model/forecast_history_series_inner.dart';
import 'package:foresignal_api/src/model/forecast_history_series_inner_points_inner.dart';
import 'package:foresignal_api/src/model/forecast_input.dart';
import 'package:foresignal_api/src/model/forecast_numeric_calibration.dart';
import 'package:foresignal_api/src/model/forecast_numeric_calibration_pit.dart';
import 'package:foresignal_api/src/model/forecast_numeric_calibration_pit_bins_inner.dart';
import 'package:foresignal_api/src/model/forecast_numeric_calibration_quantile_reliability_inner.dart';
import 'package:foresignal_api/src/model/forecast_numeric_calibration_summary.dart';
import 'package:foresignal_api/src/model/forecast_page.dart';
import 'package:foresignal_api/src/model/forecast_question.dart';
import 'package:foresignal_api/src/model/forecast_question_event.dart';
import 'package:foresignal_api/src/model/forecast_question_input.dart';
import 'package:foresignal_api/src/model/forecast_question_page.dart';
import 'package:foresignal_api/src/model/forecast_question_patch.dart';
import 'package:foresignal_api/src/model/forecast_question_summary.dart';
import 'package:foresignal_api/src/model/forecast_resolution.dart';
import 'package:foresignal_api/src/model/forecast_resolution_input.dart';
import 'package:foresignal_api/src/model/forecast_resolver.dart';
import 'package:foresignal_api/src/model/forecast_score.dart';
import 'package:foresignal_api/src/model/forecast_score_interval_hits.dart';
import 'package:foresignal_api/src/model/forecast_scorecard.dart';
import 'package:foresignal_api/src/model/forecast_series.dart';
import 'package:foresignal_api/src/model/forecast_series_point.dart';
import 'package:foresignal_api/src/model/forecast_submitted_event.dart';
import 'package:foresignal_api/src/model/forecaster.dart';
import 'package:foresignal_api/src/model/forecaster_comparison.dart';
import 'package:foresignal_api/src/model/forecaster_page.dart';
import 'package:foresignal_api/src/model/forecaster_track_record.dart';
import 'package:foresignal_api/src/model/forecaster_update.dart';
import 'package:foresignal_api/src/model/frame_item.dart';
import 'package:foresignal_api/src/model/frame_stats.dart';
import 'package:foresignal_api/src/model/geofence_ref.dart';
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:foresignal_api/src/model/get_forecast_calibration200_response.dart';
import 'package:foresignal_api/src/model/get_intel_calibration200_response.dart';
import 'package:foresignal_api/src/model/get_principal200_response.dart';
import 'package:foresignal_api/src/model/get_vapid_public_key200_response.dart';
import 'package:foresignal_api/src/model/glance.dart';
import 'package:foresignal_api/src/model/glance_activity.dart';
import 'package:foresignal_api/src/model/glance_alerts.dart';
import 'package:foresignal_api/src/model/glance_alerts_latest_inner.dart';
import 'package:foresignal_api/src/model/glance_alerts_open.dart';
import 'package:foresignal_api/src/model/glance_briefing.dart';
import 'package:foresignal_api/src/model/glance_sources.dart';
import 'package:foresignal_api/src/model/gotify_message.dart';
import 'package:foresignal_api/src/model/gotify_message_input.dart';
import 'package:foresignal_api/src/model/gps_logger_fix.dart';
import 'package:foresignal_api/src/model/graph_request.dart';
import 'package:foresignal_api/src/model/graph_result.dart';
import 'package:foresignal_api/src/model/ground_station_payload.dart';
import 'package:foresignal_api/src/model/group.dart';
import 'package:foresignal_api/src/model/group_input.dart';
import 'package:foresignal_api/src/model/group_member.dart';
import 'package:foresignal_api/src/model/group_member_input.dart';
import 'package:foresignal_api/src/model/group_members_input.dart';
import 'package:foresignal_api/src/model/group_page.dart';
import 'package:foresignal_api/src/model/group_patch.dart';
import 'package:foresignal_api/src/model/hazard_forecast.dart';
import 'package:foresignal_api/src/model/hazard_forecast_probability_field_inner.dart';
import 'package:foresignal_api/src/model/hazard_forecast_probability_range.dart';
import 'package:foresignal_api/src/model/hello_event.dart';
import 'package:foresignal_api/src/model/hf_propagation_anomaly_payload.dart';
import 'package:foresignal_api/src/model/history_frame.dart';
import 'package:foresignal_api/src/model/history_frame_removals_inner.dart';
import 'package:foresignal_api/src/model/history_frames.dart';
import 'package:foresignal_api/src/model/history_page.dart';
import 'package:foresignal_api/src/model/hold.dart';
import 'package:foresignal_api/src/model/hold_input.dart';
import 'package:foresignal_api/src/model/hold_page.dart';
import 'package:foresignal_api/src/model/hold_version_page.dart';
import 'package:foresignal_api/src/model/holding_pattern_payload.dart';
import 'package:foresignal_api/src/model/holding_pattern_payload_all_of_nearest_airport.dart';
import 'package:foresignal_api/src/model/holdover_fire_risk_payload.dart';
import 'package:foresignal_api/src/model/hypothesis.dart';
import 'package:foresignal_api/src/model/hypothesis_cap_override.dart';
import 'package:foresignal_api/src/model/hypothesis_change.dart';
import 'package:foresignal_api/src/model/hypothesis_coincidence_baseline.dart';
import 'package:foresignal_api/src/model/hypothesis_confidence_basis.dart';
import 'package:foresignal_api/src/model/hypothesis_input.dart';
import 'package:foresignal_api/src/model/hypothesis_input_prior_basis.dart';
import 'package:foresignal_api/src/model/hypothesis_patch.dart';
import 'package:foresignal_api/src/model/hypothesis_patch_probability_override.dart';
import 'package:foresignal_api/src/model/hypothesis_prior_basis.dart';
import 'package:foresignal_api/src/model/hypothesis_probability_state.dart';
import 'package:foresignal_api/src/model/hypothesis_review_input.dart';
import 'package:foresignal_api/src/model/hypothesis_review_input_assumptions_inner.dart';
import 'package:foresignal_api/src/model/hypothesis_review_input_lr_inner.dart';
import 'package:foresignal_api/src/model/hypothesis_revision.dart';
import 'package:foresignal_api/src/model/hypothesis_revision_page.dart';
import 'package:foresignal_api/src/model/hypothesis_set.dart';
import 'package:foresignal_api/src/model/hypothesis_set_draft.dart';
import 'package:foresignal_api/src/model/hypothesis_set_draft_request.dart';
import 'package:foresignal_api/src/model/hypothesis_set_draft_usage.dart';
import 'package:foresignal_api/src/model/hypothesis_set_draft_warnings_inner.dart';
import 'package:foresignal_api/src/model/hypothesis_set_event.dart';
import 'package:foresignal_api/src/model/hypothesis_set_event_band_changes_inner.dart';
import 'package:foresignal_api/src/model/hypothesis_set_input.dart';
import 'package:foresignal_api/src/model/hypothesis_set_input_ledger_question.dart';
import 'package:foresignal_api/src/model/hypothesis_set_input_ledger_question_resolver.dart';
import 'package:foresignal_api/src/model/hypothesis_set_page.dart';
import 'package:foresignal_api/src/model/hypothesis_set_patch.dart';
import 'package:foresignal_api/src/model/impact_projection_event.dart';
import 'package:foresignal_api/src/model/impact_rate.dart';
import 'package:foresignal_api/src/model/impact_rate_interval.dart';
import 'package:foresignal_api/src/model/impact_rate_page.dart';
import 'package:foresignal_api/src/model/import_media_ratings_request1.dart';
import 'package:foresignal_api/src/model/import_media_ratings_request1_rows_inner.dart';
import 'package:foresignal_api/src/model/import_report.dart';
import 'package:foresignal_api/src/model/inbox_item.dart';
import 'package:foresignal_api/src/model/inbox_page.dart';
import 'package:foresignal_api/src/model/index_changed_event.dart';
import 'package:foresignal_api/src/model/index_fit_result.dart';
import 'package:foresignal_api/src/model/index_fit_result_components_inner.dart';
import 'package:foresignal_api/src/model/index_weights_input.dart';
import 'package:foresignal_api/src/model/index_weights_proposed_event.dart';
import 'package:foresignal_api/src/model/indicator.dart';
import 'package:foresignal_api/src/model/indicator_event.dart';
import 'package:foresignal_api/src/model/indicator_input.dart';
import 'package:foresignal_api/src/model/indicator_likelihood.dart';
import 'package:foresignal_api/src/model/indicator_page.dart';
import 'package:foresignal_api/src/model/indicator_patch.dart';
import 'package:foresignal_api/src/model/indicator_signal.dart';
import 'package:foresignal_api/src/model/indicator_signal_intel_event.dart';
import 'package:foresignal_api/src/model/indicator_signal_question.dart';
import 'package:foresignal_api/src/model/indicator_signal_series.dart';
import 'package:foresignal_api/src/model/indicator_signal_series_condition.dart';
import 'package:foresignal_api/src/model/indicator_signal_situation.dart';
import 'package:foresignal_api/src/model/information_value.dart';
import 'package:foresignal_api/src/model/information_value_indicators_inner.dart';
import 'package:foresignal_api/src/model/infra_build.dart';
import 'package:foresignal_api/src/model/infra_edge.dart';
import 'package:foresignal_api/src/model/infra_edge_evidence.dart';
import 'package:foresignal_api/src/model/infra_edge_page.dart';
import 'package:foresignal_api/src/model/infra_impact_request.dart';
import 'package:foresignal_api/src/model/infra_impact_request_record.dart';
import 'package:foresignal_api/src/model/infra_impact_result.dart';
import 'package:foresignal_api/src/model/infra_impact_result_dependents_inner.dart';
import 'package:foresignal_api/src/model/infra_impact_result_dependents_inner_path_inner.dart';
import 'package:foresignal_api/src/model/infra_impact_result_summary.dart';
import 'package:foresignal_api/src/model/infra_node.dart';
import 'package:foresignal_api/src/model/infra_node_edges.dart';
import 'package:foresignal_api/src/model/infra_node_page.dart';
import 'package:foresignal_api/src/model/infra_rules.dart';
import 'package:foresignal_api/src/model/infra_rules_rules_inner.dart';
import 'package:foresignal_api/src/model/infra_status.dart';
import 'package:foresignal_api/src/model/ingest_device.dart';
import 'package:foresignal_api/src/model/ingest_device_created.dart';
import 'package:foresignal_api/src/model/ingest_device_input.dart';
import 'package:foresignal_api/src/model/ingest_device_setup.dart';
import 'package:foresignal_api/src/model/ingest_device_setup_gpslogger.dart';
import 'package:foresignal_api/src/model/ingest_device_setup_overland.dart';
import 'package:foresignal_api/src/model/ingest_device_setup_traccar.dart';
import 'package:foresignal_api/src/model/ingest_format_info.dart';
import 'package:foresignal_api/src/model/ingest_format_option.dart';
import 'package:foresignal_api/src/model/ingest_key_response.dart';
import 'package:foresignal_api/src/model/ingest_outcome.dart';
import 'package:foresignal_api/src/model/ingest_outcome_rejections_inner.dart';
import 'package:foresignal_api/src/model/ingest_policy.dart';
import 'package:foresignal_api/src/model/ingest_policy_all.dart';
import 'package:foresignal_api/src/model/ingest_policy_latest.dart';
import 'package:foresignal_api/src/model/ingest_policy_sampled.dart';
import 'package:foresignal_api/src/model/ingest_records.dart';
import 'package:foresignal_api/src/model/ingest_records_format_request.dart';
import 'package:foresignal_api/src/model/ingest_records_path_key200_response.dart';
import 'package:foresignal_api/src/model/ingest_records_path_key_request.dart';
import 'package:foresignal_api/src/model/ingest_records_request.dart';
import 'package:foresignal_api/src/model/intel_board.dart';
import 'package:foresignal_api/src/model/intel_board_band_changes_inner.dart';
import 'package:foresignal_api/src/model/intel_board_benchmarks_inner.dart';
import 'package:foresignal_api/src/model/intel_board_calibration.dart';
import 'package:foresignal_api/src/model/intel_board_calibration_ensemble.dart';
import 'package:foresignal_api/src/model/intel_board_calibration_operator.dart';
import 'package:foresignal_api/src/model/intel_board_forecasts_due_inner.dart';
import 'package:foresignal_api/src/model/intel_board_indicators_observed_inner.dart';
import 'package:foresignal_api/src/model/intel_board_latest_assessments_inner.dart';
import 'package:foresignal_api/src/model/intel_board_situations_inner.dart';
import 'package:foresignal_api/src/model/intel_calibration.dart';
import 'package:foresignal_api/src/model/intel_calibration_bins_inner.dart';
import 'package:foresignal_api/src/model/intel_calibration_curve_inner.dart';
import 'package:foresignal_api/src/model/intel_corroboration.dart';
import 'package:foresignal_api/src/model/intel_corroboration_auto_corroborate.dart';
import 'package:foresignal_api/src/model/intel_corroboration_event_quality.dart';
import 'package:foresignal_api/src/model/intel_corroboration_situation_activation.dart';
import 'package:foresignal_api/src/model/intel_event_alternative.dart';
import 'package:foresignal_api/src/model/intel_event_input.dart';
import 'package:foresignal_api/src/model/intel_event_log_entry.dart';
import 'package:foresignal_api/src/model/intel_event_member.dart';
import 'package:foresignal_api/src/model/intel_event_member_input.dart';
import 'package:foresignal_api/src/model/intel_event_member_join_reason.dart';
import 'package:foresignal_api/src/model/intel_event_merge_input.dart';
import 'package:foresignal_api/src/model/intel_event_page.dart';
import 'package:foresignal_api/src/model/intel_event_patch.dart';
import 'package:foresignal_api/src/model/intel_event_record_payload.dart';
import 'package:foresignal_api/src/model/intel_event_stream_event.dart';
import 'package:foresignal_api/src/model/intel_event_summary.dart';
import 'package:foresignal_api/src/model/intelligence_event.dart';
import 'package:foresignal_api/src/model/intelligence_event_context.dart';
import 'package:foresignal_api/src/model/intelligence_event_evidence.dart';
import 'package:foresignal_api/src/model/intelligence_event_inference.dart';
import 'package:foresignal_api/src/model/intelligence_event_speculation.dart';
import 'package:foresignal_api/src/model/interactive_chat_config.dart';
import 'package:foresignal_api/src/model/investigation.dart';
import 'package:foresignal_api/src/model/investigation_export.dart';
import 'package:foresignal_api/src/model/investigation_input.dart';
import 'package:foresignal_api/src/model/investigation_item.dart';
import 'package:foresignal_api/src/model/investigation_item_input.dart';
import 'package:foresignal_api/src/model/investigation_page.dart';
import 'package:foresignal_api/src/model/investigation_patch.dart';
import 'package:foresignal_api/src/model/jamming_cell_payload.dart';
import 'package:foresignal_api/src/model/json_feed.dart';
import 'package:foresignal_api/src/model/json_feed_authors_inner.dart';
import 'package:foresignal_api/src/model/json_feed_item.dart';
import 'package:foresignal_api/src/model/json_feed_item_authors_inner.dart';
import 'package:foresignal_api/src/model/json_feed_item_foresignal.dart';
import 'package:foresignal_api/src/model/jwk.dart';
import 'package:foresignal_api/src/model/jwks.dart';
import 'package:foresignal_api/src/model/key_assumption.dart';
import 'package:foresignal_api/src/model/key_assumption_event.dart';
import 'package:foresignal_api/src/model/key_assumption_input.dart';
import 'package:foresignal_api/src/model/key_assumption_patch.dart';
import 'package:foresignal_api/src/model/key_judgment.dart';
import 'package:foresignal_api/src/model/key_judgment_change.dart';
import 'package:foresignal_api/src/model/kind_display.dart';
import 'package:foresignal_api/src/model/kind_schema.dart';
import 'package:foresignal_api/src/model/kind_summary.dart';
import 'package:foresignal_api/src/model/kinematic_state.dart';
import 'package:foresignal_api/src/model/lead_time_stats.dart';
import 'package:foresignal_api/src/model/lead_time_stats_filters.dart';
import 'package:foresignal_api/src/model/lead_time_stats_groups_inner.dart';
import 'package:foresignal_api/src/model/lead_time_summary.dart';
import 'package:foresignal_api/src/model/ledger_question_brief.dart';
import 'package:foresignal_api/src/model/ledger_question_brief_forecasters_inner.dart';
import 'package:foresignal_api/src/model/legend.dart';
import 'package:foresignal_api/src/model/legend_tick.dart';
import 'package:foresignal_api/src/model/licence_terms.dart';
import 'package:foresignal_api/src/model/lightning_cluster_payload.dart';
import 'package:foresignal_api/src/model/list_records200_response.dart';
import 'package:foresignal_api/src/model/live_event.dart';
import 'package:foresignal_api/src/model/llm_model_info.dart';
import 'package:foresignal_api/src/model/llm_provider.dart';
import 'package:foresignal_api/src/model/llm_provider_input.dart';
import 'package:foresignal_api/src/model/llm_provider_input_options.dart';
import 'package:foresignal_api/src/model/llm_settings.dart';
import 'package:foresignal_api/src/model/llm_test_result.dart';
import 'package:foresignal_api/src/model/llm_test_result_chat.dart';
import 'package:foresignal_api/src/model/llm_test_result_embedding.dart';
import 'package:foresignal_api/src/model/llm_usage_point.dart';
import 'package:foresignal_api/src/model/llm_usage_series.dart';
import 'package:foresignal_api/src/model/loiter_payload.dart';
import 'package:foresignal_api/src/model/maintenance_run.dart';
import 'package:foresignal_api/src/model/maintenance_run_steps_inner.dart';
import 'package:foresignal_api/src/model/map_attachment_config.dart';
import 'package:foresignal_api/src/model/map_sprite_index_value.dart';
import 'package:foresignal_api/src/model/map_style.dart';
import 'package:foresignal_api/src/model/map_style_metadata.dart';
import 'package:foresignal_api/src/model/map_style_metadata_layers_inner.dart';
import 'package:foresignal_api/src/model/mapping_field.dart';
import 'package:foresignal_api/src/model/mapping_field_unit.dart';
import 'package:foresignal_api/src/model/mapping_preset.dart';
import 'package:foresignal_api/src/model/mapping_preset_setup.dart';
import 'package:foresignal_api/src/model/mapping_preview_request.dart';
import 'package:foresignal_api/src/model/mapping_preview_result.dart';
import 'package:foresignal_api/src/model/mapping_rejection.dart';
import 'package:foresignal_api/src/model/mapping_sample.dart';
import 'package:foresignal_api/src/model/mapping_suggest_request.dart';
import 'package:foresignal_api/src/model/mapping_suggest_request_hints.dart';
import 'package:foresignal_api/src/model/mapping_suggest_result.dart';
import 'package:foresignal_api/src/model/mapping_test_request.dart';
import 'package:foresignal_api/src/model/mapping_test_result.dart';
import 'package:foresignal_api/src/model/mapping_test_result_samples_inner.dart';
import 'package:foresignal_api/src/model/mapping_time_spec.dart';
import 'package:foresignal_api/src/model/mapping_time_spec_one_of.dart';
import 'package:foresignal_api/src/model/mapping_time_spec_one_of1.dart';
import 'package:foresignal_api/src/model/mapping_value_spec.dart';
import 'package:foresignal_api/src/model/mapping_value_spec_one_of.dart';
import 'package:foresignal_api/src/model/mapping_value_spec_one_of1.dart';
import 'package:foresignal_api/src/model/mapping_value_spec_one_of2.dart';
import 'package:foresignal_api/src/model/mapping_warning.dart';
import 'package:foresignal_api/src/model/mcp_auth_settings.dart';
import 'package:foresignal_api/src/model/me.dart';
import 'package:foresignal_api/src/model/me_groups_inner.dart';
import 'package:foresignal_api/src/model/me_token.dart';
import 'package:foresignal_api/src/model/media.dart';
import 'package:foresignal_api/src/model/media_fingerprint.dart';
import 'package:foresignal_api/src/model/media_fingerprint_uses_inner.dart';
import 'package:foresignal_api/src/model/media_links.dart';
import 'package:foresignal_api/src/model/media_match.dart';
import 'package:foresignal_api/src/model/media_match_request.dart';
import 'package:foresignal_api/src/model/media_match_result.dart';
import 'package:foresignal_api/src/model/media_match_result_query.dart';
import 'package:foresignal_api/src/model/media_outlet.dart';
import 'package:foresignal_api/src/model/media_outlet_page.dart';
import 'package:foresignal_api/src/model/media_outlet_patch.dart';
import 'package:foresignal_api/src/model/media_provider.dart';
import 'package:foresignal_api/src/model/media_provider_input.dart';
import 'package:foresignal_api/src/model/media_rating.dart';
import 'package:foresignal_api/src/model/media_reuse_event.dart';
import 'package:foresignal_api/src/model/mesh_channel_common_config.dart';
import 'package:foresignal_api/src/model/mesh_channel_common_config_airtime_custom.dart';
import 'package:foresignal_api/src/model/meshcore_channel_config.dart';
import 'package:foresignal_api/src/model/meshtastic_channel_config.dart';
import 'package:foresignal_api/src/model/metar_payload.dart';
import 'package:foresignal_api/src/model/metar_payload_sky_inner.dart';
import 'package:foresignal_api/src/model/metric_projection.dart';
import 'package:foresignal_api/src/model/metric_projection_labels_inner.dart';
import 'package:foresignal_api/src/model/metric_settings.dart';
import 'package:foresignal_api/src/model/mil_naval_cooccurrence_payload.dart';
import 'package:foresignal_api/src/model/model_disagreement.dart';
import 'package:foresignal_api/src/model/motion_spec.dart';
import 'package:foresignal_api/src/model/mqtt_channel_config.dart';
import 'package:foresignal_api/src/model/mqtt_connections.dart';
import 'package:foresignal_api/src/model/narrative.dart';
import 'package:foresignal_api/src/model/narrative_event.dart';
import 'package:foresignal_api/src/model/narrative_log_entry.dart';
import 'package:foresignal_api/src/model/narrative_merge_input.dart';
import 'package:foresignal_api/src/model/narrative_page.dart';
import 'package:foresignal_api/src/model/narrative_platform_stats.dart';
import 'package:foresignal_api/src/model/narrative_post.dart';
import 'package:foresignal_api/src/model/narrative_post_page.dart';
import 'package:foresignal_api/src/model/narrative_split_input.dart';
import 'package:foresignal_api/src/model/narrative_summary.dart';
import 'package:foresignal_api/src/model/narrative_timeline.dart';
import 'package:foresignal_api/src/model/narrative_timeline_series_inner.dart';
import 'package:foresignal_api/src/model/narrative_timeline_series_inner_points_inner.dart';
import 'package:foresignal_api/src/model/native_push_payload.dart';
import 'package:foresignal_api/src/model/native_push_payload_apns.dart';
import 'package:foresignal_api/src/model/native_push_payload_apns_aps.dart';
import 'package:foresignal_api/src/model/native_push_payload_apns_aps_alert.dart';
import 'package:foresignal_api/src/model/native_push_payload_apns_e.dart';
import 'package:foresignal_api/src/model/native_push_payload_fcm.dart';
import 'package:foresignal_api/src/model/native_push_payload_fcm_message.dart';
import 'package:foresignal_api/src/model/native_push_payload_fcm_message_android.dart';
import 'package:foresignal_api/src/model/native_push_payload_fcm_message_data.dart';
import 'package:foresignal_api/src/model/near.dart';
import 'package:foresignal_api/src/model/near_one_of.dart';
import 'package:foresignal_api/src/model/near_one_of1.dart';
import 'package:foresignal_api/src/model/near_one_of2.dart';
import 'package:foresignal_api/src/model/near_one_of2_anchor.dart';
import 'package:foresignal_api/src/model/network_salt_input.dart';
import 'package:foresignal_api/src/model/network_salt_status.dart';
import 'package:foresignal_api/src/model/news_article.dart';
import 'package:foresignal_api/src/model/news_article_page.dart';
import 'package:foresignal_api/src/model/news_story.dart';
import 'package:foresignal_api/src/model/news_story_outlets_inner.dart';
import 'package:foresignal_api/src/model/news_story_page.dart';
import 'package:foresignal_api/src/model/normalized_record_preview.dart';
import 'package:foresignal_api/src/model/normalized_record_preview_geometry.dart';
import 'package:foresignal_api/src/model/notification.dart';
import 'package:foresignal_api/src/model/notification_action.dart';
import 'package:foresignal_api/src/model/notification_action_params.dart';
import 'package:foresignal_api/src/model/notification_badge.dart';
import 'package:foresignal_api/src/model/notification_delivery.dart';
import 'package:foresignal_api/src/model/notification_delivery_page.dart';
import 'package:foresignal_api/src/model/notification_detail.dart';
import 'package:foresignal_api/src/model/notification_link.dart';
import 'package:foresignal_api/src/model/notification_message.dart';
import 'package:foresignal_api/src/model/notification_page.dart';
import 'package:foresignal_api/src/model/notification_preference.dart';
import 'package:foresignal_api/src/model/notification_preference_channel.dart';
import 'package:foresignal_api/src/model/notification_preference_input.dart';
import 'package:foresignal_api/src/model/notification_preference_input_channel.dart';
import 'package:foresignal_api/src/model/notification_preferences_input.dart';
import 'package:foresignal_api/src/model/notification_read_request.dart';
import 'package:foresignal_api/src/model/notification_read_request_one_of.dart';
import 'package:foresignal_api/src/model/notification_read_request_one_of1.dart';
import 'package:foresignal_api/src/model/notification_settings.dart';
import 'package:foresignal_api/src/model/notification_settings_chatops.dart';
import 'package:foresignal_api/src/model/notification_webhook_message.dart';
import 'package:foresignal_api/src/model/ntfy_message.dart';
import 'package:foresignal_api/src/model/ntfy_publish_request.dart';
import 'package:foresignal_api/src/model/nws_alert_payload.dart';
import 'package:foresignal_api/src/model/nws_forecast_payload.dart';
import 'package:foresignal_api/src/model/nws_forecast_payload_hazards_inner.dart';
import 'package:foresignal_api/src/model/object_acl.dart';
import 'package:foresignal_api/src/model/object_acl_entry.dart';
import 'package:foresignal_api/src/model/object_acl_entry_input.dart';
import 'package:foresignal_api/src/model/object_acl_input.dart';
import 'package:foresignal_api/src/model/object_action.dart';
import 'package:foresignal_api/src/model/object_envelope.dart';
import 'package:foresignal_api/src/model/object_envelope_links.dart';
import 'package:foresignal_api/src/model/object_envelope_page.dart';
import 'package:foresignal_api/src/model/object_envelope_time.dart';
import 'package:foresignal_api/src/model/object_resolve_request.dart';
import 'package:foresignal_api/src/model/object_resolve_result.dart';
import 'package:foresignal_api/src/model/observability_coverage.dart';
import 'package:foresignal_api/src/model/observability_estimate.dart';
import 'package:foresignal_api/src/model/observation_link.dart';
import 'package:foresignal_api/src/model/observation_link_link_reason.dart';
import 'package:foresignal_api/src/model/observer.dart';
import 'package:foresignal_api/src/model/ogc_collection.dart';
import 'package:foresignal_api/src/model/ogc_collection200_response.dart';
import 'package:foresignal_api/src/model/ogc_collection_extent.dart';
import 'package:foresignal_api/src/model/ogc_collection_extent_spatial.dart';
import 'package:foresignal_api/src/model/ogc_collection_extent_temporal.dart';
import 'package:foresignal_api/src/model/ogc_collections.dart';
import 'package:foresignal_api/src/model/ogc_conformance.dart';
import 'package:foresignal_api/src/model/ogc_coverage_collection.dart';
import 'package:foresignal_api/src/model/ogc_coverage_collection_data_queries.dart';
import 'package:foresignal_api/src/model/ogc_coverage_collection_data_queries_area.dart';
import 'package:foresignal_api/src/model/ogc_coverage_collection_data_queries_instances.dart';
import 'package:foresignal_api/src/model/ogc_coverage_collection_data_queries_position.dart';
import 'package:foresignal_api/src/model/ogc_coverage_collection_extent.dart';
import 'package:foresignal_api/src/model/ogc_coverage_collection_extent_spatial.dart';
import 'package:foresignal_api/src/model/ogc_coverage_collection_extent_temporal.dart';
import 'package:foresignal_api/src/model/ogc_feature.dart';
import 'package:foresignal_api/src/model/ogc_feature_collection.dart';
import 'package:foresignal_api/src/model/ogc_feature_properties.dart';
import 'package:foresignal_api/src/model/ogc_landing_page.dart';
import 'package:foresignal_api/src/model/ogc_link.dart';
import 'package:foresignal_api/src/model/ogc_queryables.dart';
import 'package:foresignal_api/src/model/ogc_tile_matrix_set.dart';
import 'package:foresignal_api/src/model/ogc_tile_matrix_set_list.dart';
import 'package:foresignal_api/src/model/ogc_tile_matrix_set_list_tile_matrix_sets_inner.dart';
import 'package:foresignal_api/src/model/ogc_tile_matrix_set_tile_matrices_inner.dart';
import 'package:foresignal_api/src/model/ogc_tile_set.dart';
import 'package:foresignal_api/src/model/ogc_tile_set_list.dart';
import 'package:foresignal_api/src/model/ogc_tile_set_list_tilesets_inner.dart';
import 'package:foresignal_api/src/model/ogc_tile_set_tile_matrix_set_limits_inner.dart';
import 'package:foresignal_api/src/model/opaque_chronicle_link.dart';
import 'package:foresignal_api/src/model/originator.dart';
import 'package:foresignal_api/src/model/osm_and_location.dart';
import 'package:foresignal_api/src/model/osm_and_location_location.dart';
import 'package:foresignal_api/src/model/osm_and_location_location_battery.dart';
import 'package:foresignal_api/src/model/osm_and_location_location_coords.dart';
import 'package:foresignal_api/src/model/osm_and_query.dart';
import 'package:foresignal_api/src/model/osm_infrastructure_payload.dart';
import 'package:foresignal_api/src/model/outbound_settings.dart';
import 'package:foresignal_api/src/model/outlet_consensus.dart';
import 'package:foresignal_api/src/model/outlet_consensus_providers_inner.dart';
import 'package:foresignal_api/src/model/outlook_published_event.dart';
import 'package:foresignal_api/src/model/outlook_window.dart';
import 'package:foresignal_api/src/model/overland_ack.dart';
import 'package:foresignal_api/src/model/overland_batch.dart';
import 'package:foresignal_api/src/model/overland_batch_locations_inner.dart';
import 'package:foresignal_api/src/model/overland_batch_locations_inner_properties.dart';
import 'package:foresignal_api/src/model/own_tracks_message.dart';
import 'package:foresignal_api/src/model/ownership_chain.dart';
import 'package:foresignal_api/src/model/ownership_chain_node.dart';
import 'package:foresignal_api/src/model/ownership_chain_node_paths_inner.dart';
import 'package:foresignal_api/src/model/ownership_chain_request.dart';
import 'package:foresignal_api/src/model/pack_spec.dart';
import 'package:foresignal_api/src/model/pack_spec_raster.dart';
import 'package:foresignal_api/src/model/pack_spec_vector.dart';
import 'package:foresignal_api/src/model/package_manifest.dart';
import 'package:foresignal_api/src/model/package_manifest_authors_inner.dart';
import 'package:foresignal_api/src/model/package_manifest_sender.dart';
import 'package:foresignal_api/src/model/package_received_event.dart';
import 'package:foresignal_api/src/model/package_recipient.dart';
import 'package:foresignal_api/src/model/pairing_claim.dart';
import 'package:foresignal_api/src/model/pairing_code.dart';
import 'package:foresignal_api/src/model/pairing_code_created.dart';
import 'package:foresignal_api/src/model/pairing_code_input.dart';
import 'package:foresignal_api/src/model/param_spec.dart';
import 'package:foresignal_api/src/model/peer.dart';
import 'package:foresignal_api/src/model/peer_create.dart';
import 'package:foresignal_api/src/model/peer_ingest_key.dart';
import 'package:foresignal_api/src/model/peer_page.dart';
import 'package:foresignal_api/src/model/peer_selection.dart';
import 'package:foresignal_api/src/model/peer_test_result.dart';
import 'package:foresignal_api/src/model/peer_update.dart';
import 'package:foresignal_api/src/model/persist_reason_counts.dart';
import 'package:foresignal_api/src/model/place.dart';
import 'package:foresignal_api/src/model/place_input.dart';
import 'package:foresignal_api/src/model/place_input_attributes.dart';
import 'package:foresignal_api/src/model/place_pack_request.dart';
import 'package:foresignal_api/src/model/place_page.dart';
import 'package:foresignal_api/src/model/place_weather.dart';
import 'package:foresignal_api/src/model/place_weather_aurora.dart';
import 'package:foresignal_api/src/model/place_weather_cell.dart';
import 'package:foresignal_api/src/model/place_weather_digest.dart';
import 'package:foresignal_api/src/model/place_weather_digest_next.dart';
import 'package:foresignal_api/src/model/place_weather_digest_next_gfs.dart';
import 'package:foresignal_api/src/model/place_weather_digest_next_hrrr.dart';
import 'package:foresignal_api/src/model/place_weather_digest_next_nws.dart';
import 'package:foresignal_api/src/model/place_weather_digest_now.dart';
import 'package:foresignal_api/src/model/place_weather_digest_now_alerts_inner.dart';
import 'package:foresignal_api/src/model/place_weather_digest_now_alerts_status.dart';
import 'package:foresignal_api/src/model/place_weather_digest_now_lightning.dart';
import 'package:foresignal_api/src/model/place_weather_digest_now_radar.dart';
import 'package:foresignal_api/src/model/place_weather_digest_period.dart';
import 'package:foresignal_api/src/model/place_weather_digest_period_hazards_entered_inner.dart';
import 'package:foresignal_api/src/model/place_weather_forecast.dart';
import 'package:foresignal_api/src/model/place_weather_forecast_gfs.dart';
import 'package:foresignal_api/src/model/place_weather_forecast_nws.dart';
import 'package:foresignal_api/src/model/place_weather_hail.dart';
import 'package:foresignal_api/src/model/place_weather_heat.dart';
import 'package:foresignal_api/src/model/place_weather_lightning.dart';
import 'package:foresignal_api/src/model/place_weather_marine.dart';
import 'package:foresignal_api/src/model/place_weather_precip.dart';
import 'package:foresignal_api/src/model/place_weather_radar.dart';
import 'package:foresignal_api/src/model/place_weather_response.dart';
import 'package:foresignal_api/src/model/place_weather_smoke.dart';
import 'package:foresignal_api/src/model/place_weather_uv.dart';
import 'package:foresignal_api/src/model/places_aoi.dart';
import 'package:foresignal_api/src/model/plugin.dart';
import 'package:foresignal_api/src/model/plugin_list.dart';
import 'package:foresignal_api/src/model/plugin_settings.dart';
import 'package:foresignal_api/src/model/plugin_update.dart';
import 'package:foresignal_api/src/model/pol_deviation.dart';
import 'package:foresignal_api/src/model/pol_deviation_stay.dart';
import 'package:foresignal_api/src/model/pol_place.dart';
import 'package:foresignal_api/src/model/pol_place_input.dart';
import 'package:foresignal_api/src/model/pol_place_page.dart';
import 'package:foresignal_api/src/model/pol_stay.dart';
import 'package:foresignal_api/src/model/pol_stay_page.dart';
import 'package:foresignal_api/src/model/pol_subject.dart';
import 'package:foresignal_api/src/model/pol_subject_input.dart';
import 'package:foresignal_api/src/model/pol_subject_input_params.dart';
import 'package:foresignal_api/src/model/pol_subject_page.dart';
import 'package:foresignal_api/src/model/population_pack_import.dart';
import 'package:foresignal_api/src/model/population_pack_import_cells.dart';
import 'package:foresignal_api/src/model/population_pack_import_request.dart';
import 'package:foresignal_api/src/model/port_call_payload.dart';
import 'package:foresignal_api/src/model/precision.dart';
import 'package:foresignal_api/src/model/precision_components_inner.dart';
import 'package:foresignal_api/src/model/precursor.dart';
import 'package:foresignal_api/src/model/precursor_batch.dart';
import 'package:foresignal_api/src/model/precursor_batch_page.dart';
import 'package:foresignal_api/src/model/precursor_candidate.dart';
import 'package:foresignal_api/src/model/precursor_candidate_input.dart';
import 'package:foresignal_api/src/model/precursor_candidate_page.dart';
import 'package:foresignal_api/src/model/precursor_event.dart';
import 'package:foresignal_api/src/model/precursor_event_precursor.dart';
import 'package:foresignal_api/src/model/precursor_history_inner.dart';
import 'package:foresignal_api/src/model/precursor_lag_distribution.dart';
import 'package:foresignal_api/src/model/precursor_page.dart';
import 'package:foresignal_api/src/model/precursor_patch.dart';
import 'package:foresignal_api/src/model/precursor_side.dart';
import 'package:foresignal_api/src/model/precursor_spatial.dart';
import 'package:foresignal_api/src/model/precursor_test.dart';
import 'package:foresignal_api/src/model/precursor_test_antecedent.dart';
import 'package:foresignal_api/src/model/precursor_test_antecedent_one_of.dart';
import 'package:foresignal_api/src/model/precursor_test_antecedent_one_of1.dart';
import 'package:foresignal_api/src/model/precursor_test_antecedent_one_of2.dart';
import 'package:foresignal_api/src/model/precursor_test_antecedent_one_of3.dart';
import 'package:foresignal_api/src/model/precursor_test_input.dart';
import 'package:foresignal_api/src/model/precursor_test_page.dart';
import 'package:foresignal_api/src/model/precursor_test_result.dart';
import 'package:foresignal_api/src/model/precursor_test_result_hit_rate_ci.dart';
import 'package:foresignal_api/src/model/precursor_test_result_lag.dart';
import 'package:foresignal_api/src/model/precursor_test_result_multiple_testing.dart';
import 'package:foresignal_api/src/model/precursor_test_result_reverse.dart';
import 'package:foresignal_api/src/model/precursor_test_run_request.dart';
import 'package:foresignal_api/src/model/precursor_test_spatial.dart';
import 'package:foresignal_api/src/model/precursor_test_spatial_one_of.dart';
import 'package:foresignal_api/src/model/precursor_test_spatial_one_of1.dart';
import 'package:foresignal_api/src/model/precursor_window_stats.dart';
import 'package:foresignal_api/src/model/precursor_window_stats_reverse.dart';
import 'package:foresignal_api/src/model/presence_event.dart';
import 'package:foresignal_api/src/model/presence_event_change.dart';
import 'package:foresignal_api/src/model/presence_event_clients_inner.dart';
import 'package:foresignal_api/src/model/principal.dart';
import 'package:foresignal_api/src/model/principal_input.dart';
import 'package:foresignal_api/src/model/principal_patch.dart';
import 'package:foresignal_api/src/model/principal_summary.dart';
import 'package:foresignal_api/src/model/principal_summary_page.dart';
import 'package:foresignal_api/src/model/prob_severe_payload.dart';
import 'package:foresignal_api/src/model/prob_severe_payload_motion.dart';
import 'package:foresignal_api/src/model/probe_result.dart';
import 'package:foresignal_api/src/model/probe_result_requests_inner.dart';
import 'package:foresignal_api/src/model/problem.dart';
import 'package:foresignal_api/src/model/problem_errors_inner.dart';
import 'package:foresignal_api/src/model/problem_references.dart';
import 'package:foresignal_api/src/model/product.dart';
import 'package:foresignal_api/src/model/product_export_request.dart';
import 'package:foresignal_api/src/model/product_legend.dart';
import 'package:foresignal_api/src/model/product_links.dart';
import 'package:foresignal_api/src/model/product_loop.dart';
import 'package:foresignal_api/src/model/product_loop_frames_inner.dart';
import 'package:foresignal_api/src/model/product_page.dart';
import 'package:foresignal_api/src/model/product_render.dart';
import 'package:foresignal_api/src/model/product_spatial.dart';
import 'package:foresignal_api/src/model/product_status.dart';
import 'package:foresignal_api/src/model/product_temporal.dart';
import 'package:foresignal_api/src/model/product_temporal_retention.dart';
import 'package:foresignal_api/src/model/product_temporal_retention_ladder_inner.dart';
import 'package:foresignal_api/src/model/product_temporal_runs_inner.dart';
import 'package:foresignal_api/src/model/product_timeline.dart';
import 'package:foresignal_api/src/model/projected_impact.dart';
import 'package:foresignal_api/src/model/projected_impact_dependency_paths_inner.dart';
import 'package:foresignal_api/src/model/projected_impact_exposed_assets_inner.dart';
import 'package:foresignal_api/src/model/projected_impact_inputs_inner.dart';
import 'package:foresignal_api/src/model/projected_impact_interval.dart';
import 'package:foresignal_api/src/model/projected_impact_page.dart';
import 'package:foresignal_api/src/model/projected_impact_question_window.dart';
import 'package:foresignal_api/src/model/projected_impact_skipped_inner.dart';
import 'package:foresignal_api/src/model/prompt_template.dart';
import 'package:foresignal_api/src/model/prompt_template_list.dart';
import 'package:foresignal_api/src/model/prompt_template_summary.dart';
import 'package:foresignal_api/src/model/propagation_edge.dart';
import 'package:foresignal_api/src/model/propagation_graph.dart';
import 'package:foresignal_api/src/model/propagation_graph_cross_platform_order_inner.dart';
import 'package:foresignal_api/src/model/propagation_graph_earliest_by_platform_inner.dart';
import 'package:foresignal_api/src/model/propagation_graph_first_news_pickup.dart';
import 'package:foresignal_api/src/model/propagation_node.dart';
import 'package:foresignal_api/src/model/proposed_patch.dart';
import 'package:foresignal_api/src/model/proposed_patch_sanity_checks.dart';
import 'package:foresignal_api/src/model/protected_resource_metadata.dart';
import 'package:foresignal_api/src/model/provenance.dart';
import 'package:foresignal_api/src/model/provenance_chain_entry.dart';
import 'package:foresignal_api/src/model/provenance_hop.dart';
import 'package:foresignal_api/src/model/purge_report.dart';
import 'package:foresignal_api/src/model/purge_report_citing_documents.dart';
import 'package:foresignal_api/src/model/purge_request.dart';
import 'package:foresignal_api/src/model/push_device.dart';
import 'package:foresignal_api/src/model/push_device_input.dart';
import 'package:foresignal_api/src/model/push_device_update.dart';
import 'package:foresignal_api/src/model/push_filters.dart';
import 'package:foresignal_api/src/model/push_payload.dart';
import 'package:foresignal_api/src/model/push_payload_actions_inner.dart';
import 'package:foresignal_api/src/model/push_payload_links.dart';
import 'package:foresignal_api/src/model/push_provider.dart';
import 'package:foresignal_api/src/model/push_provider_input.dart';
import 'package:foresignal_api/src/model/push_provider_input_config.dart';
import 'package:foresignal_api/src/model/push_provider_input_secrets.dart';
import 'package:foresignal_api/src/model/push_provider_test_request.dart';
import 'package:foresignal_api/src/model/push_settings.dart';
import 'package:foresignal_api/src/model/push_subscription.dart';
import 'package:foresignal_api/src/model/push_subscription_input.dart';
import 'package:foresignal_api/src/model/push_subscription_input_subscription.dart';
import 'package:foresignal_api/src/model/push_subscription_input_subscription_keys.dart';
import 'package:foresignal_api/src/model/push_subscription_verify.dart';
import 'package:foresignal_api/src/model/push_test_result.dart';
import 'package:foresignal_api/src/model/quality.dart';
import 'package:foresignal_api/src/model/quantiles.dart';
import 'package:foresignal_api/src/model/query_history200_response.dart';
import 'package:foresignal_api/src/model/question_annulment.dart';
import 'package:foresignal_api/src/model/question_links.dart';
import 'package:foresignal_api/src/model/question_outcomes.dart';
import 'package:foresignal_api/src/model/quiet_hours.dart';
import 'package:foresignal_api/src/model/radar_cell_payload.dart';
import 'package:foresignal_api/src/model/radar_cell_payload_motion.dart';
import 'package:foresignal_api/src/model/raster_frame.dart';
import 'package:foresignal_api/src/model/raster_frame_event.dart';
import 'package:foresignal_api/src/model/raster_frame_forecast_run.dart';
import 'package:foresignal_api/src/model/raster_frame_observation.dart';
import 'package:foresignal_api/src/model/raster_frame_page.dart';
import 'package:foresignal_api/src/model/raster_frame_page_prefetch.dart';
import 'package:foresignal_api/src/model/rate_limit_settings.dart';
import 'package:foresignal_api/src/model/rate_limit_settings_stream.dart';
import 'package:foresignal_api/src/model/ratings_import_result.dart';
import 'package:foresignal_api/src/model/ratings_import_result_errors_inner.dart';
import 'package:foresignal_api/src/model/read_restriction.dart';
import 'package:foresignal_api/src/model/received_package.dart';
import 'package:foresignal_api/src/model/received_package_contents_inner.dart';
import 'package:foresignal_api/src/model/received_package_page.dart';
import 'package:foresignal_api/src/model/received_package_sender.dart';
import 'package:foresignal_api/src/model/recipe_backtest_result.dart';
import 'package:foresignal_api/src/model/recipe_backtest_result_events_inner.dart';
import 'package:foresignal_api/src/model/recipe_backtest_result_overlap.dart';
import 'package:foresignal_api/src/model/recipe_test_request.dart';
import 'package:foresignal_api/src/model/recipe_test_result.dart';
import 'package:foresignal_api/src/model/record.dart';
import 'package:foresignal_api/src/model/record_evidence.dart';
import 'package:foresignal_api/src/model/record_mapping.dart';
import 'package:foresignal_api/src/model/record_mapping_filter_inner.dart';
import 'package:foresignal_api/src/model/record_mapping_geometry.dart';
import 'package:foresignal_api/src/model/record_mapping_geometry_one_of.dart';
import 'package:foresignal_api/src/model/record_mapping_geometry_one_of1.dart';
import 'package:foresignal_api/src/model/record_mapping_geometry_one_of2.dart';
import 'package:foresignal_api/src/model/record_mapping_pairs.dart';
import 'package:foresignal_api/src/model/record_mapping_quality.dart';
import 'package:foresignal_api/src/model/record_mapping_spread.dart';
import 'package:foresignal_api/src/model/record_mapping_tombstone.dart';
import 'package:foresignal_api/src/model/record_page.dart';
import 'package:foresignal_api/src/model/record_version.dart';
import 'package:foresignal_api/src/model/record_version_page.dart';
import 'package:foresignal_api/src/model/reference_area.dart';
import 'package:foresignal_api/src/model/reference_area_outline.dart';
import 'package:foresignal_api/src/model/reference_dataset.dart';
import 'package:foresignal_api/src/model/reference_import.dart';
import 'package:foresignal_api/src/model/reference_import_page.dart';
import 'package:foresignal_api/src/model/reference_import_request.dart';
import 'package:foresignal_api/src/model/reference_import_request_params.dart';
import 'package:foresignal_api/src/model/reference_load_request.dart';
import 'package:foresignal_api/src/model/reference_status.dart';
import 'package:foresignal_api/src/model/region_near.dart';
import 'package:foresignal_api/src/model/region_profile.dart';
import 'package:foresignal_api/src/model/region_tags.dart';
import 'package:foresignal_api/src/model/regional_index_def.dart';
import 'package:foresignal_api/src/model/regional_index_def_components_inner.dart';
import 'package:foresignal_api/src/model/regional_index_def_floors_inner.dart';
import 'package:foresignal_api/src/model/regional_index_def_input.dart';
import 'package:foresignal_api/src/model/regional_index_def_page.dart';
import 'package:foresignal_api/src/model/regional_index_def_skill.dart';
import 'package:foresignal_api/src/model/regional_index_detail.dart';
import 'package:foresignal_api/src/model/regional_index_value.dart';
import 'package:foresignal_api/src/model/regional_index_value_components_inner.dart';
import 'package:foresignal_api/src/model/regional_outlook.dart';
import 'package:foresignal_api/src/model/regional_outlook_page.dart';
import 'package:foresignal_api/src/model/regional_outlook_skill.dart';
import 'package:foresignal_api/src/model/registration_token.dart';
import 'package:foresignal_api/src/model/registration_token_created.dart';
import 'package:foresignal_api/src/model/registration_token_input.dart';
import 'package:foresignal_api/src/model/related_object.dart';
import 'package:foresignal_api/src/model/related_objects.dart';
import 'package:foresignal_api/src/model/relation.dart';
import 'package:foresignal_api/src/model/relation_input.dart';
import 'package:foresignal_api/src/model/relation_provenance.dart';
import 'package:foresignal_api/src/model/remote_media_accepted.dart';
import 'package:foresignal_api/src/model/remote_media_meta.dart';
import 'package:foresignal_api/src/model/remote_raster_bundle_accepted.dart';
import 'package:foresignal_api/src/model/remote_raster_bundle_meta.dart';
import 'package:foresignal_api/src/model/rendezvous_payload.dart';
import 'package:foresignal_api/src/model/replay_result.dart';
import 'package:foresignal_api/src/model/report.dart';
import 'package:foresignal_api/src/model/report_block.dart';
import 'package:foresignal_api/src/model/report_block_base.dart';
import 'package:foresignal_api/src/model/report_block_one_of.dart';
import 'package:foresignal_api/src/model/report_block_one_of1.dart';
import 'package:foresignal_api/src/model/report_block_one_of10.dart';
import 'package:foresignal_api/src/model/report_block_one_of10_all_of_attributions_inner.dart';
import 'package:foresignal_api/src/model/report_block_one_of1_all_of_judgments_inner.dart';
import 'package:foresignal_api/src/model/report_block_one_of2.dart';
import 'package:foresignal_api/src/model/report_block_one_of2_all_of_items_inner.dart';
import 'package:foresignal_api/src/model/report_block_one_of3.dart';
import 'package:foresignal_api/src/model/report_block_one_of4.dart';
import 'package:foresignal_api/src/model/report_block_one_of4_all_of_items_inner.dart';
import 'package:foresignal_api/src/model/report_block_one_of5.dart';
import 'package:foresignal_api/src/model/report_block_one_of6.dart';
import 'package:foresignal_api/src/model/report_block_one_of6_all_of_questions_inner.dart';
import 'package:foresignal_api/src/model/report_block_one_of7.dart';
import 'package:foresignal_api/src/model/report_block_one_of7_all_of_items_inner.dart';
import 'package:foresignal_api/src/model/report_block_one_of8.dart';
import 'package:foresignal_api/src/model/report_block_one_of9.dart';
import 'package:foresignal_api/src/model/report_bundle.dart';
import 'package:foresignal_api/src/model/report_bundle_attachments_inner.dart';
import 'package:foresignal_api/src/model/report_draft_request.dart';
import 'package:foresignal_api/src/model/report_event.dart';
import 'package:foresignal_api/src/model/report_input.dart';
import 'package:foresignal_api/src/model/report_page.dart';
import 'package:foresignal_api/src/model/report_patch.dart';
import 'package:foresignal_api/src/model/report_retract_request.dart';
import 'package:foresignal_api/src/model/report_revision.dart';
import 'package:foresignal_api/src/model/report_revision_page.dart';
import 'package:foresignal_api/src/model/report_revision_renditions.dart';
import 'package:foresignal_api/src/model/report_send.dart';
import 'package:foresignal_api/src/model/report_send_input.dart';
import 'package:foresignal_api/src/model/report_send_recipients_inner.dart';
import 'package:foresignal_api/src/model/report_share_link_input.dart';
import 'package:foresignal_api/src/model/report_source.dart';
import 'package:foresignal_api/src/model/reset_event.dart';
import 'package:foresignal_api/src/model/resolver_area.dart';
import 'package:foresignal_api/src/model/resync_event.dart';
import 'package:foresignal_api/src/model/retention_run_result.dart';
import 'package:foresignal_api/src/model/retention_run_result_consumer_clamp.dart';
import 'package:foresignal_api/src/model/retention_run_result_per_source_value.dart';
import 'package:foresignal_api/src/model/retention_settings.dart';
import 'package:foresignal_api/src/model/retention_settings_social.dart';
import 'package:foresignal_api/src/model/retention_settings_stats.dart';
import 'package:foresignal_api/src/model/revision_driver.dart';
import 'package:foresignal_api/src/model/rule_backtest_result.dart';
import 'package:foresignal_api/src/model/rule_backtest_result_actual.dart';
import 'package:foresignal_api/src/model/rule_backtest_result_anomaly.dart';
import 'package:foresignal_api/src/model/rule_backtest_result_overlap.dart';
import 'package:foresignal_api/src/model/rule_backtest_result_per_day_inner.dart';
import 'package:foresignal_api/src/model/rule_backtest_result_samples_inner.dart';
import 'package:foresignal_api/src/model/rule_backtest_result_top_entities_inner.dart';
import 'package:foresignal_api/src/model/rule_backtest_result_variants_inner.dart';
import 'package:foresignal_api/src/model/rule_backtest_result_window.dart';
import 'package:foresignal_api/src/model/rule_quality.dart';
import 'package:foresignal_api/src/model/rule_quality_daily.dart';
import 'package:foresignal_api/src/model/rule_quality_daily_feedback.dart';
import 'package:foresignal_api/src/model/rule_quality_daily_signals.dart';
import 'package:foresignal_api/src/model/rule_quality_precision_ci.dart';
import 'package:foresignal_api/src/model/rule_quality_signals.dart';
import 'package:foresignal_api/src/model/sample_batch_response.dart';
import 'package:foresignal_api/src/model/sample_position.dart';
import 'package:foresignal_api/src/model/sample_request.dart';
import 'package:foresignal_api/src/model/sample_request_points_inner.dart';
import 'package:foresignal_api/src/model/sample_request_points_inner_one_of.dart';
import 'package:foresignal_api/src/model/sample_request_points_inner_one_of1.dart';
import 'package:foresignal_api/src/model/sample_response.dart';
import 'package:foresignal_api/src/model/sample_track_request.dart';
import 'package:foresignal_api/src/model/sample_track_request_points_inner.dart';
import 'package:foresignal_api/src/model/sample_track_response.dart';
import 'package:foresignal_api/src/model/sample_track_response_points_inner.dart';
import 'package:foresignal_api/src/model/sample_value.dart';
import 'package:foresignal_api/src/model/sample_value_derived.dart';
import 'package:foresignal_api/src/model/satellite_pass_payload.dart';
import 'package:foresignal_api/src/model/satellite_pass_payload_products_inner.dart';
import 'package:foresignal_api/src/model/satellite_pass_payload_rf.dart';
import 'package:foresignal_api/src/model/satellite_pass_payload_rf_snr_by_elevation_inner.dart';
import 'package:foresignal_api/src/model/saved_search.dart';
import 'package:foresignal_api/src/model/saved_search_hit.dart';
import 'package:foresignal_api/src/model/saved_search_input.dart';
import 'package:foresignal_api/src/model/saved_search_matches_event.dart';
import 'package:foresignal_api/src/model/saved_search_page.dart';
import 'package:foresignal_api/src/model/saved_search_patch.dart';
import 'package:foresignal_api/src/model/saved_search_query.dart';
import 'package:foresignal_api/src/model/saved_search_query_area.dart';
import 'package:foresignal_api/src/model/saved_search_query_time_window.dart';
import 'package:foresignal_api/src/model/saved_search_result_page.dart';
import 'package:foresignal_api/src/model/saved_view.dart';
import 'package:foresignal_api/src/model/saved_view_input.dart';
import 'package:foresignal_api/src/model/saved_view_page.dart';
import 'package:foresignal_api/src/model/scorecard_entry.dart';
import 'package:foresignal_api/src/model/scorecard_entry_bias.dart';
import 'package:foresignal_api/src/model/scorecard_entry_interval_coverage.dart';
import 'package:foresignal_api/src/model/scorecard_entry_murphy.dart';
import 'package:foresignal_api/src/model/scorecard_entry_recalibration.dart';
import 'package:foresignal_api/src/model/scorecard_entry_skill.dart';
import 'package:foresignal_api/src/model/search200_response.dart';
import 'package:foresignal_api/src/model/search_reindex_state.dart';
import 'package:foresignal_api/src/model/search_result.dart';
import 'package:foresignal_api/src/model/search_results.dart';
import 'package:foresignal_api/src/model/search_status.dart';
import 'package:foresignal_api/src/model/security_settings.dart';
import 'package:foresignal_api/src/model/sensor.dart';
import 'package:foresignal_api/src/model/sensor_all_of_capabilities.dart';
import 'package:foresignal_api/src/model/sensor_all_of_passes.dart';
import 'package:foresignal_api/src/model/sensor_discovery_result.dart';
import 'package:foresignal_api/src/model/series.dart';
import 'package:foresignal_api/src/model/series_catalog_entry.dart';
import 'package:foresignal_api/src/model/series_data.dart';
import 'package:foresignal_api/src/model/series_forecast.dart';
import 'package:foresignal_api/src/model/series_forecast_forecasts_inner.dart';
import 'package:foresignal_api/src/model/series_forecast_model.dart';
import 'package:foresignal_api/src/model/series_forecast_model_backtest.dart';
import 'package:foresignal_api/src/model/series_forecast_model_backtest_results_inner.dart';
import 'package:foresignal_api/src/model/series_info.dart';
import 'package:foresignal_api/src/model/series_page.dart';
import 'package:foresignal_api/src/model/series_point.dart';
import 'package:foresignal_api/src/model/series_point_page.dart';
import 'package:foresignal_api/src/model/series_sample.dart';
import 'package:foresignal_api/src/model/server_settings.dart';
import 'package:foresignal_api/src/model/settings_bundle.dart';
import 'package:foresignal_api/src/model/settings_bundle_channels_inner.dart';
import 'package:foresignal_api/src/model/settings_bundle_watchlists_inner.dart';
import 'package:foresignal_api/src/model/settings_warning.dart';
import 'package:foresignal_api/src/model/share_link.dart';
import 'package:foresignal_api/src/model/share_link_create.dart';
import 'package:foresignal_api/src/model/share_link_created.dart';
import 'package:foresignal_api/src/model/share_link_page.dart';
import 'package:foresignal_api/src/model/share_received_event.dart';
import 'package:foresignal_api/src/model/share_request.dart';
import 'package:foresignal_api/src/model/shared_content.dart';
import 'package:foresignal_api/src/model/shared_content_links.dart';
import 'package:foresignal_api/src/model/shared_item.dart';
import 'package:foresignal_api/src/model/shared_item_page.dart';
import 'package:foresignal_api/src/model/signature_verifier_config.dart';
import 'package:foresignal_api/src/model/signing_key_info.dart';
import 'package:foresignal_api/src/model/sink.dart';
import 'package:foresignal_api/src/model/sink_cloud_event.dart';
import 'package:foresignal_api/src/model/sink_cloud_event_data.dart';
import 'package:foresignal_api/src/model/sink_create.dart';
import 'package:foresignal_api/src/model/sink_filter.dart';
import 'package:foresignal_api/src/model/sink_page.dart';
import 'package:foresignal_api/src/model/sink_status.dart';
import 'package:foresignal_api/src/model/sink_status_clients.dart';
import 'package:foresignal_api/src/model/sink_status_clients_peers_inner.dart';
import 'package:foresignal_api/src/model/sink_status_denied.dart';
import 'package:foresignal_api/src/model/sink_test_request.dart';
import 'package:foresignal_api/src/model/sink_test_result.dart';
import 'package:foresignal_api/src/model/sink_type.dart';
import 'package:foresignal_api/src/model/sink_update.dart';
import 'package:foresignal_api/src/model/situation.dart';
import 'package:foresignal_api/src/model/situation_assessment_request.dart';
import 'package:foresignal_api/src/model/situation_brief_status.dart';
import 'package:foresignal_api/src/model/situation_changes.dart';
import 'package:foresignal_api/src/model/situation_changes_area.dart';
import 'package:foresignal_api/src/model/situation_changes_key_values_inner.dart';
import 'package:foresignal_api/src/model/situation_changes_new_members.dart';
import 'package:foresignal_api/src/model/situation_changes_severity.dart';
import 'package:foresignal_api/src/model/situation_changes_status.dart';
import 'package:foresignal_api/src/model/situation_event.dart';
import 'package:foresignal_api/src/model/situation_evidence.dart';
import 'package:foresignal_api/src/model/situation_evidence_corroboration.dart';
import 'package:foresignal_api/src/model/situation_evidence_families_inner.dart';
import 'package:foresignal_api/src/model/situation_evidence_member_counts.dart';
import 'package:foresignal_api/src/model/situation_inference.dart';
import 'package:foresignal_api/src/model/situation_input.dart';
import 'package:foresignal_api/src/model/situation_join_reason.dart';
import 'package:foresignal_api/src/model/situation_join_reason_components.dart';
import 'package:foresignal_api/src/model/situation_link_decision.dart';
import 'package:foresignal_api/src/model/situation_link_decision_candidates_inner.dart';
import 'package:foresignal_api/src/model/situation_linker_test_request.dart';
import 'package:foresignal_api/src/model/situation_linker_test_result.dart';
import 'package:foresignal_api/src/model/situation_member.dart';
import 'package:foresignal_api/src/model/situation_member_input.dart';
import 'package:foresignal_api/src/model/situation_member_page.dart';
import 'package:foresignal_api/src/model/situation_merge_input.dart';
import 'package:foresignal_api/src/model/situation_page.dart';
import 'package:foresignal_api/src/model/situation_patch.dart';
import 'package:foresignal_api/src/model/situation_social_section.dart';
import 'package:foresignal_api/src/model/situation_social_section_attention_gap.dart';
import 'package:foresignal_api/src/model/situation_social_section_narratives_inner.dart';
import 'package:foresignal_api/src/model/situation_speculation.dart';
import 'package:foresignal_api/src/model/situation_split_input.dart';
import 'package:foresignal_api/src/model/situation_summary.dart';
import 'package:foresignal_api/src/model/situation_timeline_entry.dart';
import 'package:foresignal_api/src/model/situation_timeline_page.dart';
import 'package:foresignal_api/src/model/smoke_polygon_payload.dart';
import 'package:foresignal_api/src/model/snapshot.dart';
import 'package:foresignal_api/src/model/snapshot_input.dart';
import 'package:foresignal_api/src/model/snooze_input.dart';
import 'package:foresignal_api/src/model/social_author.dart';
import 'package:foresignal_api/src/model/social_collection_path.dart';
import 'package:foresignal_api/src/model/social_comparison.dart';
import 'package:foresignal_api/src/model/social_comparison_contradiction.dart';
import 'package:foresignal_api/src/model/social_comparison_detectability.dart';
import 'package:foresignal_api/src/model/social_comparison_event.dart';
import 'package:foresignal_api/src/model/social_comparison_page.dart';
import 'package:foresignal_api/src/model/social_comparison_revision.dart';
import 'package:foresignal_api/src/model/social_coverage.dart';
import 'package:foresignal_api/src/model/social_coverage_bucket.dart';
import 'package:foresignal_api/src/model/social_coverage_page.dart';
import 'package:foresignal_api/src/model/social_cue.dart';
import 'package:foresignal_api/src/model/social_cue_event.dart';
import 'package:foresignal_api/src/model/social_cue_external_results_inner.dart';
import 'package:foresignal_api/src/model/social_cue_input.dart';
import 'package:foresignal_api/src/model/social_cue_page.dart';
import 'package:foresignal_api/src/model/social_cue_query.dart';
import 'package:foresignal_api/src/model/social_cue_results_inner.dart';
import 'package:foresignal_api/src/model/social_cue_trigger.dart';
import 'package:foresignal_api/src/model/social_engagement.dart';
import 'package:foresignal_api/src/model/social_lead_times.dart';
import 'package:foresignal_api/src/model/social_physical_evidence.dart';
import 'package:foresignal_api/src/model/social_platform.dart';
import 'package:foresignal_api/src/model/social_platform_credentials.dart';
import 'package:foresignal_api/src/model/social_platform_deletion_sync.dart';
import 'package:foresignal_api/src/model/social_platform_price.dart';
import 'package:foresignal_api/src/model/social_platform_settings.dart';
import 'package:foresignal_api/src/model/social_platform_settings_options.dart';
import 'package:foresignal_api/src/model/social_platform_state_event.dart';
import 'package:foresignal_api/src/model/social_platform_stream.dart';
import 'package:foresignal_api/src/model/social_platform_stream_last_gap.dart';
import 'package:foresignal_api/src/model/social_platform_terms.dart';
import 'package:foresignal_api/src/model/social_post.dart';
import 'package:foresignal_api/src/model/social_post_geo.dart';
import 'package:foresignal_api/src/model/social_post_link.dart';
import 'package:foresignal_api/src/model/social_post_media.dart';
import 'package:foresignal_api/src/model/social_post_page.dart';
import 'package:foresignal_api/src/model/social_post_payload.dart';
import 'package:foresignal_api/src/model/social_quota.dart';
import 'package:foresignal_api/src/model/social_settings.dart';
import 'package:foresignal_api/src/model/social_settings_attention_gap.dart';
import 'package:foresignal_api/src/model/social_settings_compare.dart';
import 'package:foresignal_api/src/model/social_settings_compare_default_window.dart';
import 'package:foresignal_api/src/model/social_settings_compare_family_windows_value.dart';
import 'package:foresignal_api/src/model/social_settings_cues.dart';
import 'package:foresignal_api/src/model/social_settings_cues_triggers.dart';
import 'package:foresignal_api/src/model/social_settings_cues_xai_x_search.dart';
import 'package:foresignal_api/src/model/social_settings_track_record.dart';
import 'package:foresignal_api/src/model/social_settings_track_record_overrides_inner.dart';
import 'package:foresignal_api/src/model/social_settings_velocity.dart';
import 'package:foresignal_api/src/model/social_spend.dart';
import 'package:foresignal_api/src/model/social_velocity_event.dart';
import 'package:foresignal_api/src/model/social_velocity_event_scope.dart';
import 'package:foresignal_api/src/model/social_versus_observed.dart';
import 'package:foresignal_api/src/model/social_versus_observed_lead_times_inner.dart';
import 'package:foresignal_api/src/model/social_versus_observed_narratives_inner.dart';
import 'package:foresignal_api/src/model/social_volume.dart';
import 'package:foresignal_api/src/model/social_volume_point.dart';
import 'package:foresignal_api/src/model/social_volume_scope.dart';
import 'package:foresignal_api/src/model/social_volume_series.dart';
import 'package:foresignal_api/src/model/source_access.dart';
import 'package:foresignal_api/src/model/source_descriptor.dart';
import 'package:foresignal_api/src/model/source_detail.dart';
import 'package:foresignal_api/src/model/source_health.dart';
import 'package:foresignal_api/src/model/source_relevance.dart';
import 'package:foresignal_api/src/model/source_relevance_analysis.dart';
import 'package:foresignal_api/src/model/source_relevance_analysis_shared.dart';
import 'package:foresignal_api/src/model/source_relevance_analysis_template.dart';
import 'package:foresignal_api/src/model/source_relevance_request.dart';
import 'package:foresignal_api/src/model/source_settings.dart';
import 'package:foresignal_api/src/model/source_settings_update.dart';
import 'package:foresignal_api/src/model/source_storage.dart';
import 'package:foresignal_api/src/model/source_summary.dart';
import 'package:foresignal_api/src/model/spc_outlook_payload.dart';
import 'package:foresignal_api/src/model/stat_interval.dart';
import 'package:foresignal_api/src/model/stats_grid.dart';
import 'package:foresignal_api/src/model/stats_grid_features_inner.dart';
import 'package:foresignal_api/src/model/stats_grid_features_inner_properties.dart';
import 'package:foresignal_api/src/model/stats_request.dart';
import 'package:foresignal_api/src/model/stats_response.dart';
import 'package:foresignal_api/src/model/stats_response_thresholds_inner.dart';
import 'package:foresignal_api/src/model/stats_rollup_rebuild.dart';
import 'package:foresignal_api/src/model/stats_rollup_rebuild_request.dart';
import 'package:foresignal_api/src/model/stats_series.dart';
import 'package:foresignal_api/src/model/stats_series_series_inner.dart';
import 'package:foresignal_api/src/model/stats_series_series_inner_points_inner.dart';
import 'package:foresignal_api/src/model/stats_top.dart';
import 'package:foresignal_api/src/model/stats_top_items_inner.dart';
import 'package:foresignal_api/src/model/stix_bundle.dart';
import 'package:foresignal_api/src/model/storm_report_payload.dart';
import 'package:foresignal_api/src/model/storm_swath_payload.dart';
import 'package:foresignal_api/src/model/storm_track_payload.dart';
import 'package:foresignal_api/src/model/storm_track_payload_cell.dart';
import 'package:foresignal_api/src/model/storm_track_payload_history_inner.dart';
import 'package:foresignal_api/src/model/storm_track_payload_lightning_jump.dart';
import 'package:foresignal_api/src/model/storm_track_payload_motion.dart';
import 'package:foresignal_api/src/model/storm_track_payload_nowcast_inner.dart';
import 'package:foresignal_api/src/model/storm_track_payload_trend.dart';
import 'package:foresignal_api/src/model/stream_alerts200_response.dart';
import 'package:foresignal_api/src/model/stream_changes200_response.dart';
import 'package:foresignal_api/src/model/stream_events200_response.dart';
import 'package:foresignal_api/src/model/stream_federation200_response.dart';
import 'package:foresignal_api/src/model/stream_notifications200_response.dart';
import 'package:foresignal_api/src/model/stream_ticket.dart';
import 'package:foresignal_api/src/model/stream_ticket_request.dart';
import 'package:foresignal_api/src/model/stream_view200_response.dart';
import 'package:foresignal_api/src/model/structure_diff.dart';
import 'package:foresignal_api/src/model/structure_diff_content_type_changed.dart';
import 'package:foresignal_api/src/model/structure_diff_delimiter_changed.dart';
import 'package:foresignal_api/src/model/structure_diff_presence_dropped_inner.dart';
import 'package:foresignal_api/src/model/structure_diff_rename_candidates_inner.dart';
import 'package:foresignal_api/src/model/structure_diff_type_changed_inner.dart';
import 'package:foresignal_api/src/model/style.dart';
import 'package:foresignal_api/src/model/style_category.dart';
import 'package:foresignal_api/src/model/style_fragment.dart';
import 'package:foresignal_api/src/model/style_fragment_metadata.dart';
import 'package:foresignal_api/src/model/style_meaning.dart';
import 'package:foresignal_api/src/model/style_page.dart';
import 'package:foresignal_api/src/model/style_special.dart';
import 'package:foresignal_api/src/model/style_stop.dart';
import 'package:foresignal_api/src/model/system_info.dart';
import 'package:foresignal_api/src/model/system_info_archive.dart';
import 'package:foresignal_api/src/model/system_info_catch_up.dart';
import 'package:foresignal_api/src/model/system_info_database.dart';
import 'package:foresignal_api/src/model/system_info_exports.dart';
import 'package:foresignal_api/src/model/system_info_history.dart';
import 'package:foresignal_api/src/model/system_info_history_tiers_inner.dart';
import 'package:foresignal_api/src/model/system_info_holds.dart';
import 'package:foresignal_api/src/model/system_info_ingest_writer.dart';
import 'package:foresignal_api/src/model/system_info_limits.dart';
import 'package:foresignal_api/src/model/system_info_llm.dart';
import 'package:foresignal_api/src/model/system_info_mcp.dart';
import 'package:foresignal_api/src/model/system_info_mcp_transports.dart';
import 'package:foresignal_api/src/model/system_info_notifications.dart';
import 'package:foresignal_api/src/model/system_info_raster_formats.dart';
import 'package:foresignal_api/src/model/system_info_raster_formats_webp.dart';
import 'package:foresignal_api/src/model/system_info_records.dart';
import 'package:foresignal_api/src/model/system_info_scheduler.dart';
import 'package:foresignal_api/src/model/system_info_search.dart';
import 'package:foresignal_api/src/model/system_info_security.dart';
import 'package:foresignal_api/src/model/system_info_sinks.dart';
import 'package:foresignal_api/src/model/system_info_sources.dart';
import 'package:foresignal_api/src/model/system_info_streams.dart';
import 'package:foresignal_api/src/model/system_info_warnings_inner.dart';
import 'package:foresignal_api/src/model/system_rasters.dart';
import 'package:foresignal_api/src/model/system_rasters_caches_value.dart';
import 'package:foresignal_api/src/model/system_rasters_glm.dart';
import 'package:foresignal_api/src/model/system_rasters_render_queue.dart';
import 'package:foresignal_api/src/model/taf_payload.dart';
import 'package:foresignal_api/src/model/taf_payload_forecast_inner.dart';
import 'package:foresignal_api/src/model/tag.dart';
import 'package:foresignal_api/src/model/tag_assign_result.dart';
import 'package:foresignal_api/src/model/tag_assign_result_skipped_inner.dart';
import 'package:foresignal_api/src/model/tag_assignment.dart';
import 'package:foresignal_api/src/model/tag_input.dart';
import 'package:foresignal_api/src/model/tag_page.dart';
import 'package:foresignal_api/src/model/tag_patch.dart';
import 'package:foresignal_api/src/model/tag_refs_request.dart';
import 'package:foresignal_api/src/model/taxii_api_root.dart';
import 'package:foresignal_api/src/model/taxii_collection.dart';
import 'package:foresignal_api/src/model/taxii_collections.dart';
import 'package:foresignal_api/src/model/taxii_discovery.dart';
import 'package:foresignal_api/src/model/taxii_envelope.dart';
import 'package:foresignal_api/src/model/tile_json.dart';
import 'package:foresignal_api/src/model/tile_json_vector_layers_inner.dart';
import 'package:foresignal_api/src/model/tile_key.dart';
import 'package:foresignal_api/src/model/tile_key_create.dart';
import 'package:foresignal_api/src/model/tile_key_created.dart';
import 'package:foresignal_api/src/model/tile_key_page.dart';
import 'package:foresignal_api/src/model/tile_layer_index.dart';
import 'package:foresignal_api/src/model/tile_layer_index_layers_inner.dart';
import 'package:foresignal_api/src/model/time_range.dart';
import 'package:foresignal_api/src/model/timeline.dart';
import 'package:foresignal_api/src/model/timeline_alerts_inner.dart';
import 'package:foresignal_api/src/model/timeline_bucket.dart';
import 'package:foresignal_api/src/model/timeline_tick.dart';
import 'package:foresignal_api/src/model/timeline_tick_frames_inner.dart';
import 'package:foresignal_api/src/model/token_restrictions.dart';
import 'package:foresignal_api/src/model/touch_event.dart';
import 'package:foresignal_api/src/model/track.dart';
import 'package:foresignal_api/src/model/track_properties.dart';
import 'package:foresignal_api/src/model/transit_catalog_entry.dart';
import 'package:foresignal_api/src/model/transit_catalog_page.dart';
import 'package:foresignal_api/src/model/trigger_mute_input.dart';
import 'package:foresignal_api/src/model/trigger_mute_input_one_of.dart';
import 'package:foresignal_api/src/model/trigger_mute_input_one_of1.dart';
import 'package:foresignal_api/src/model/trigger_rule.dart';
import 'package:foresignal_api/src/model/trigger_rule_all_of_feedback_counts.dart';
import 'package:foresignal_api/src/model/trigger_rule_input.dart';
import 'package:foresignal_api/src/model/trigger_rule_input_auto_resolve.dart';
import 'package:foresignal_api/src/model/trigger_rule_input_conditions_inner.dart';
import 'package:foresignal_api/src/model/trigger_rule_input_window.dart';
import 'package:foresignal_api/src/model/trigger_test_result.dart';
import 'package:foresignal_api/src/model/trigger_test_result_warnings_inner.dart';
import 'package:foresignal_api/src/model/trigger_test_result_window_evaluations_inner.dart';
import 'package:foresignal_api/src/model/trip.dart';
import 'package:foresignal_api/src/model/trip_check_request.dart';
import 'package:foresignal_api/src/model/trip_hazard.dart';
import 'package:foresignal_api/src/model/trip_input.dart';
import 'package:foresignal_api/src/model/trip_page.dart';
import 'package:foresignal_api/src/model/trip_run.dart';
import 'package:foresignal_api/src/model/trip_run_page.dart';
import 'package:foresignal_api/src/model/trip_schedule.dart';
import 'package:foresignal_api/src/model/tuning_log_entry.dart';
import 'package:foresignal_api/src/model/tuning_log_page.dart';
import 'package:foresignal_api/src/model/tuning_suggestion.dart';
import 'package:foresignal_api/src/model/tuning_suggestion_apply.dart';
import 'package:foresignal_api/src/model/tuning_suggestion_dismiss.dart';
import 'package:foresignal_api/src/model/tuning_suggestion_expected_effect.dart';
import 'package:foresignal_api/src/model/tuning_suggestion_list.dart';
import 'package:foresignal_api/src/model/tuning_suggestion_record.dart';
import 'package:foresignal_api/src/model/update_token_request.dart';
import 'package:foresignal_api/src/model/usage_point.dart';
import 'package:foresignal_api/src/model/usage_series.dart';
import 'package:foresignal_api/src/model/uv_forecast_payload.dart';
import 'package:foresignal_api/src/model/uv_forecast_payload_hours_inner.dart';
import 'package:foresignal_api/src/model/velocity_json_component.dart';
import 'package:foresignal_api/src/model/velocity_json_component_header.dart';
import 'package:foresignal_api/src/model/watch_pair.dart';
import 'package:foresignal_api/src/model/watchdog_settings.dart';
import 'package:foresignal_api/src/model/watchdog_settings_canary.dart';
import 'package:foresignal_api/src/model/watchlist.dart';
import 'package:foresignal_api/src/model/watchlist_import_result.dart';
import 'package:foresignal_api/src/model/watchlist_import_result_invalid_inner.dart';
import 'package:foresignal_api/src/model/watchlist_input.dart';
import 'package:foresignal_api/src/model/watchlist_item.dart';
import 'package:foresignal_api/src/model/watchlist_item_input.dart';
import 'package:foresignal_api/src/model/watchlist_item_page.dart';
import 'package:foresignal_api/src/model/watchlist_items_input.dart';
import 'package:foresignal_api/src/model/watchlist_page.dart';
import 'package:foresignal_api/src/model/weather_context.dart';
import 'package:foresignal_api/src/model/weather_context_lightning.dart';
import 'package:foresignal_api/src/model/weather_context_precip.dart';
import 'package:foresignal_api/src/model/weather_context_radar.dart';
import 'package:foresignal_api/src/model/weather_context_radar_nearest_cell.dart';
import 'package:foresignal_api/src/model/weather_context_radar_nearest_cell_motion.dart';
import 'package:foresignal_api/src/model/weather_context_smoke.dart';
import 'package:foresignal_api/src/model/weather_context_warnings.dart';
import 'package:foresignal_api/src/model/weather_context_warnings_items_inner.dart';
import 'package:foresignal_api/src/model/weather_context_wind.dart';
import 'package:foresignal_api/src/model/weather_settings.dart';
import 'package:foresignal_api/src/model/webhook_envelope.dart';
import 'package:foresignal_api/src/model/window_spec_common.dart';
import 'package:foresignal_api/src/model/window_state.dart';
import 'package:foresignal_api/src/model/window_state_list.dart';
import 'package:foresignal_api/src/model/window_state_payload.dart';
import 'package:foresignal_api/src/model/window_status.dart';
import 'package:foresignal_api/src/model/window_status_states.dart';
import 'package:foresignal_api/src/model/workspace.dart';
import 'package:foresignal_api/src/model/workspace_context.dart';
import 'package:foresignal_api/src/model/workspace_context_playback.dart';
import 'package:foresignal_api/src/model/workspace_context_state.dart';
import 'package:foresignal_api/src/model/workspace_input.dart';
import 'package:foresignal_api/src/model/workspace_page.dart';
import 'package:foresignal_api/src/model/workspace_patch.dart';
import 'package:foresignal_api/src/model/workspace_share_input.dart';
import 'package:foresignal_api/src/model/workspace_viewport.dart';
import 'package:foresignal_api/src/model/wx_area_payload.dart';

final _regList = RegExp(r'^List<(.*)>$');
final _regSet = RegExp(r'^Set<(.*)>$');
final _regMap = RegExp(r'^Map<String,(.*)>$');

  ReturnType deserialize<ReturnType, BaseType>(dynamic value, String targetType, {bool growable= true}) {
      switch (targetType) {
        case 'String':
          return '$value' as ReturnType;
        case 'int':
          return (value is int ? value : int.parse('$value')) as ReturnType;
        case 'bool':
          if (value is bool) {
            return value as ReturnType;
          }
          final valueString = '$value'.toLowerCase();
          return (valueString == 'true' || valueString == '1') as ReturnType;
        case 'double':
          return (value is double ? value : double.parse('$value')) as ReturnType;
        case 'AbsenceWindow':
          return AbsenceWindow.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AccessGrantInput':
          return AccessGrantInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AccessRequest':
          return AccessRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AccessRequestGrant':
          return AccessRequestGrant.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AccessRequestInput':
          return AccessRequestInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AccessRequestOfferShare':
          return AccessRequestOfferShare.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AccessRequestPage':
          return AccessRequestPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AchEvidence':
          return AchEvidence.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AchEvidenceInput':
          return AchEvidenceInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AchMatrix':
          return AchMatrix.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AchMatrixHypothesesInner':
          return AchMatrixHypothesesInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ActionResult':
          return ActionResult.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ActiveWindow':
          return ActiveWindow.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ActiveWindowWindowsInner':
          return ActiveWindowWindowsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AdminTokenResponse':
          return AdminTokenResponse.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AircraftFusionPayload':
          return AircraftFusionPayload.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AircraftRegistrationPayload':
          return AircraftRegistrationPayload.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AisGapPayload':
          return AisGapPayload.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Alert':
          return Alert.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AlertContext':
          return AlertContext.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AlertContextAnchor':
          return AlertContextAnchor.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AlertContextNear':
          return AlertContextNear.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AlertEvent':
          return AlertEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AlertFeedbackInput':
          return AlertFeedbackInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AlertPage':
          return AlertPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AlertRecordSnapshot':
          return AlertRecordSnapshot.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AlertSignal':
          return AlertSignal.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AlertTransitionInput':
          return AlertTransitionInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AlertmanagerWebhook':
          return AlertmanagerWebhook.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AlertmanagerWebhookAlertsInner':
          return AlertmanagerWebhookAlertsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AllowExternalSnapshot200Response':
          return AllowExternalSnapshot200Response.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AllowedChat':
          return AllowedChat.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Analog':
          return Analog.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnalogOutcomes':
          return AnalogOutcomes.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnalogOutcomesHypothesisOutcome':
          return AnalogOutcomesHypothesisOutcome.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnalogQuery':
          return AnalogQuery.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnalogResult':
          return AnalogResult.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnalogResultOutcomeDistribution':
          return AnalogResultOutcomeDistribution.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnalogResultSignature':
          return AnalogResultSignature.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnalogResultTarget':
          return AnalogResultTarget.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnalysisCallback':
          return AnalysisCallback.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnalysisCycle':
          return AnalysisCycle.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnalysisCycleInput':
          return AnalysisCycleInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnalysisCycleInputSchedule':
          return AnalysisCycleInputSchedule.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnalysisCyclePage':
          return AnalysisCyclePage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnalysisManifest':
          return AnalysisManifest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnalysisManifestBuild':
          return AnalysisManifestBuild.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnalysisManifestInputs':
          return AnalysisManifestInputs.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnalysisManifestModel':
          return AnalysisManifestModel.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnalysisManifestModelTemplatesInner':
          return AnalysisManifestModelTemplatesInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnalysisRequestResult':
          return AnalysisRequestResult.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnalysisRequestSubmission':
          return AnalysisRequestSubmission.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnalysisSeries':
          return AnalysisSeries.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnalysisSeriesInput':
          return AnalysisSeriesInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnalysisSeriesPage':
          return AnalysisSeriesPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnalystClaim':
          return AnalystClaim.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnalystCursor':
          return AnalystCursor.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnalystEvaluation':
          return AnalystEvaluation.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnalystEvaluationProfilesInner':
          return AnalystEvaluationProfilesInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnalystEvaluationProfilesInnerTokensPerDayInner':
          return AnalystEvaluationProfilesInnerTokensPerDayInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnalystFeedback':
          return AnalystFeedback.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnalystFeedbackInput':
          return AnalystFeedbackInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnalystNote':
          return AnalystNote.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnalystNoteEvent':
          return AnalystNoteEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnalystNotePage':
          return AnalystNotePage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnalystNotePatch':
          return AnalystNotePatch.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnalystProfile':
          return AnalystProfile.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnalystProfileInput':
          return AnalystProfileInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnalystProfileSchedule':
          return AnalystProfileSchedule.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnalystProfileScope':
          return AnalystProfileScope.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnalystProposal':
          return AnalystProposal.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnalystProposalDecision':
          return AnalystProposalDecision.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnalystProposalEvent':
          return AnalystProposalEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnalystProposalPage':
          return AnalystProposalPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnalystRun':
          return AnalystRun.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnalystRunInputCounts':
          return AnalystRunInputCounts.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnalystRunPage':
          return AnalystRunPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnalyticFinding':
          return AnalyticFinding.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnalyticFindingDismissal':
          return AnalyticFindingDismissal.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnalyticFindingEvidence':
          return AnalyticFindingEvidence.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnalyticFindingInference':
          return AnalyticFindingInference.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Annotation':
          return Annotation.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnnotationInput':
          return AnnotationInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnnotationPage':
          return AnnotationPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnnotationPatch':
          return AnnotationPatch.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnnotationReplyInput':
          return AnnotationReplyInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnnotationResolveInput':
          return AnnotationResolveInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnnotationThread':
          return AnnotationThread.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnomalyBaseline':
          return AnomalyBaseline.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnomalyBaselineSlot':
          return AnomalyBaselineSlot.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnomalyObservation':
          return AnomalyObservation.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnomalyObservationBaseline':
          return AnomalyObservationBaseline.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnomalyObservationEvidence':
          return AnomalyObservationEvidence.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnomalyObservationEvidenceFetchLogRangesInner':
          return AnomalyObservationEvidenceFetchLogRangesInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnomalyObservationEvidenceSamplesInner':
          return AnomalyObservationEvidenceSamplesInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnomalyObservationPage':
          return AnomalyObservationPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnomalyRecordPayload':
          return AnomalyRecordPayload.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AnomalyWindow':
          return AnomalyWindow.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ApiToken':
          return ApiToken.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ApiTokenCreated':
          return ApiTokenCreated.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ApiTokenInput':
          return ApiTokenInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ApnsProviderConfig':
          return ApnsProviderConfig.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ApproachCondition':
          return ApproachCondition.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ApproachConditionTarget':
          return ApproachConditionTarget.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ArchivePartition':
          return ArchivePartition.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ArchivePartitionFilesInner':
          return ArchivePartitionFilesInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ArchivePartitionPage':
          return ArchivePartitionPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ArchiveSettings':
          return ArchiveSettings.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ArchiveSettingsS3':
          return ArchiveSettingsS3.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ArchiveSettingsSecrets':
          return ArchiveSettingsSecrets.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AskAction':
          return AskAction.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AskCitation':
          return AskCitation.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AskDeltaEvent':
          return AskDeltaEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AskMessage':
          return AskMessage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AskProgressEvent':
          return AskProgressEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AskRequest':
          return AskRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AskResponse':
          return AskResponse.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AskResponseUsage':
          return AskResponseUsage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AskScope':
          return AskScope.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AskStream200Response':
          return AskStream200Response.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AskThread':
          return AskThread.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AskThreadPage':
          return AskThreadPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AskThreadPageItemsInner':
          return AskThreadPageItemsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AskWarningEvent':
          return AskWarningEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Assessment':
          return Assessment.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AssessmentBundle':
          return AssessmentBundle.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AssessmentBundleObjectsInner':
          return AssessmentBundleObjectsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AssessmentCoverage':
          return AssessmentCoverage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AssessmentCoverageSituationsInner':
          return AssessmentCoverageSituationsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AssessmentFeedback':
          return AssessmentFeedback.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AssessmentFeedbackInput':
          return AssessmentFeedbackInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AssessmentPage':
          return AssessmentPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AssessmentPromptTemplatesInner':
          return AssessmentPromptTemplatesInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AssessmentPublishedEvent':
          return AssessmentPublishedEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AssessmentSectionsInner':
          return AssessmentSectionsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Attachment':
          return Attachment.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AttentionGapEvent':
          return AttentionGapEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AttentionTopic':
          return AttentionTopic.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AttentionTopicInput':
          return AttentionTopicInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AttentionTopicInputWikiArticlesInner':
          return AttentionTopicInputWikiArticlesInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AttentionTopicPage':
          return AttentionTopicPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AttentionTopicSocial':
          return AttentionTopicSocial.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AttentionTopicSocialAccountsInner':
          return AttentionTopicSocialAccountsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AttentionTopicSocialChannel':
          return AttentionTopicSocialChannel.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Attribution':
          return Attribution.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AttributionSummary':
          return AttributionSummary.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AuditEvent':
          return AuditEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AuditEventPage':
          return AuditEventPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'AutoResolve':
          return AutoResolve.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'BackfillJob':
          return BackfillJob.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'BacktestCaveat':
          return BacktestCaveat.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'BacktestJob':
          return BacktestJob.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'BacktestJobPage':
          return BacktestJobPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'BacktestJobResult':
          return BacktestJobResult.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'BacktestRequest':
          return BacktestRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'BaseRate':
          return BaseRate.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'BaseRateDefinition':
          return BaseRateDefinition.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'BaseRateDefinitionCondition':
          return BaseRateDefinitionCondition.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'BaseRateInput':
          return BaseRateInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'BaseRateLicencesInner':
          return BaseRateLicencesInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'BaseRatePage':
          return BaseRatePage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'BaseRateRefreshResult':
          return BaseRateRefreshResult.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'BaseRateUnconditional':
          return BaseRateUnconditional.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'BaselineRebuildResult':
          return BaselineRebuildResult.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'BenchmarkCase':
          return BenchmarkCase.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'BenchmarkCaseExpectations':
          return BenchmarkCaseExpectations.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'BenchmarkCaseExpectationsFindingsInner':
          return BenchmarkCaseExpectationsFindingsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'BenchmarkCaseExpectationsPrecursorsInner':
          return BenchmarkCaseExpectationsPrecursorsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'BenchmarkCaseExpectationsQuestionsInner':
          return BenchmarkCaseExpectationsQuestionsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'BenchmarkCaseInput':
          return BenchmarkCaseInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'BenchmarkCasePage':
          return BenchmarkCasePage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'BenchmarkComponentResult':
          return BenchmarkComponentResult.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'BenchmarkComponentResultScenariosInner':
          return BenchmarkComponentResultScenariosInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'BenchmarkComponentResultThresholdsInner':
          return BenchmarkComponentResultThresholdsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'BenchmarkRun':
          return BenchmarkRun.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'BenchmarkRunInput':
          return BenchmarkRunInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'BenchmarkRunPage':
          return BenchmarkRunPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'BenchmarkStatus':
          return BenchmarkStatus.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'BenchmarkStatusComponentsInner':
          return BenchmarkStatusComponentsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'BenchmarkStatusComponentsInnerGating':
          return BenchmarkStatusComponentsInnerGating.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Briefing':
          return Briefing.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'BriefingInput':
          return BriefingInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'BriefingInputAudio':
          return BriefingInputAudio.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'BriefingRun':
          return BriefingRun.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'BriefingRunAudio':
          return BriefingRunAudio.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'BriefingRunPage':
          return BriefingRunPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'BriefingScope':
          return BriefingScope.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Budget':
          return Budget.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CableProximityRiskPayload':
          return CableProximityRiskPayload.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CanonicalEvent':
          return CanonicalEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CanonicalEventPage':
          return CanonicalEventPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CaptureMode':
          return CaptureMode.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CapturedSample':
          return CapturedSample.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Caveat':
          return Caveat.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CelestrakGpElement':
          return CelestrakGpElement.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Change':
          return Change.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChangeEvent':
          return ChangeEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChangePage':
          return ChangePage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Channel':
          return Channel.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChannelActionsConfig':
          return ChannelActionsConfig.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChannelAirtime':
          return ChannelAirtime.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChannelInput':
          return ChannelInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChannelInputConfig':
          return ChannelInputConfig.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChannelInteractiveStatus':
          return ChannelInteractiveStatus.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChannelInteractiveStatusChatsInner':
          return ChannelInteractiveStatusChatsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChannelTestResult':
          return ChannelTestResult.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChannelType':
          return ChannelType.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChannelTypeCapabilities':
          return ChannelTypeCapabilities.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChannelTypeConfigFieldsInner':
          return ChannelTypeConfigFieldsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChannelWarningsInner':
          return ChannelWarningsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChokepointActivityPayload':
          return ChokepointActivityPayload.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChokepointUturnPayload':
          return ChokepointUturnPayload.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Chronicle':
          return Chronicle.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleAccessEvent':
          return ChronicleAccessEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleActivateRequest':
          return ChronicleActivateRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleActivateRequestBackfill':
          return ChronicleActivateRequestBackfill.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleArchive':
          return ChronicleArchive.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleArea':
          return ChronicleArea.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleCluster':
          return ChronicleCluster.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleClusterMembersInner':
          return ChronicleClusterMembersInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleClusterPage':
          return ChronicleClusterPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleEstimate':
          return ChronicleEstimate.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleEstimateLayersInner':
          return ChronicleEstimateLayersInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleExportRequest':
          return ChronicleExportRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleExternalStreamSelector':
          return ChronicleExternalStreamSelector.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleFrameIndex':
          return ChronicleFrameIndex.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleFrameIndexFramesInner':
          return ChronicleFrameIndexFramesInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleInput':
          return ChronicleInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleLayer':
          return ChronicleLayer.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleLayerEstimate':
          return ChronicleLayerEstimate.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleLayerInput':
          return ChronicleLayerInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleLayerPatch':
          return ChronicleLayerPatch.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleLayerSelector':
          return ChronicleLayerSelector.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleLayerSelectorOneOf':
          return ChronicleLayerSelectorOneOf.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleLayerSelectorOneOf1':
          return ChronicleLayerSelectorOneOf1.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleLayerSelectorOneOf2':
          return ChronicleLayerSelectorOneOf2.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleLayerSelectorOneOf3':
          return ChronicleLayerSelectorOneOf3.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleLayerSelectorOneOf4':
          return ChronicleLayerSelectorOneOf4.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleLayerSelectorOneOf5':
          return ChronicleLayerSelectorOneOf5.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleLayerSelectorOneOf6':
          return ChronicleLayerSelectorOneOf6.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleLayerSelectorOneOf7':
          return ChronicleLayerSelectorOneOf7.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleLayerSelectorOneOf8':
          return ChronicleLayerSelectorOneOf8.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleLayerStats':
          return ChronicleLayerStats.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleLink':
          return ChronicleLink.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleLinkFoundEvent':
          return ChronicleLinkFoundEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleLinkScores':
          return ChronicleLinkScores.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleLinkScoresEvidence':
          return ChronicleLinkScoresEvidence.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChroniclePage':
          return ChroniclePage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChroniclePatch':
          return ChroniclePatch.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleRecordVersion':
          return ChronicleRecordVersion.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleRelatedPage':
          return ChronicleRelatedPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleRelatedPageItemsInner':
          return ChronicleRelatedPageItemsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleSeries':
          return ChronicleSeries.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleSketchBatch':
          return ChronicleSketchBatch.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleSketchBatchAccessDecisionsInner':
          return ChronicleSketchBatchAccessDecisionsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleSketchBatchAccessRequestsInner':
          return ChronicleSketchBatchAccessRequestsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleSketchBatchResult':
          return ChronicleSketchBatchResult.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleSketchBatchResultRejectedInner':
          return ChronicleSketchBatchResultRejectedInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleSketchPackage':
          return ChronicleSketchPackage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleSketchPackageCaptureSpan':
          return ChronicleSketchPackageCaptureSpan.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleSocialNarrativesSelector':
          return ChronicleSocialNarrativesSelector.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleSocialVolumeSelector':
          return ChronicleSocialVolumeSelector.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleSocialVolumeSelectorScopesInner':
          return ChronicleSocialVolumeSelectorScopesInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleState':
          return ChronicleState.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleStateLayersInner':
          return ChronicleStateLayersInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleStateLayersInnerRollupsInner':
          return ChronicleStateLayersInnerRollupsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleStatusEvent':
          return ChronicleStatusEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleSuggestRequest':
          return ChronicleSuggestRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleSuggestion':
          return ChronicleSuggestion.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleSuggestionEstimate':
          return ChronicleSuggestionEstimate.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleSuggestionEstimateLayersInner':
          return ChronicleSuggestionEstimateLayersInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleSummary':
          return ChronicleSummary.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleSummaryGapsInner':
          return ChronicleSummaryGapsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleSummaryLayersInner':
          return ChronicleSummaryLayersInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleSummaryLayersInnerSeriesInner':
          return ChronicleSummaryLayersInnerSeriesInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleSummaryStorage':
          return ChronicleSummaryStorage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleSummaryTopEntitiesInner':
          return ChronicleSummaryTopEntitiesInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleThreadItem':
          return ChronicleThreadItem.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleThreadItemGap':
          return ChronicleThreadItemGap.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleThreadPage':
          return ChronicleThreadPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ChronicleThreadReadRequest':
          return ChronicleThreadReadRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Claim':
          return Claim.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ClaimEventTime':
          return ClaimEventTime.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ClaimExtractedEvent':
          return ClaimExtractedEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ClaimPage':
          return ClaimPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ClaimPlace':
          return ClaimPlace.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ClaimReputation':
          return ClaimReputation.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ClientLinks':
          return ClientLinks.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ClientPresence':
          return ClientPresence.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ClientPresenceInput':
          return ClientPresenceInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CloudEvent':
          return CloudEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ClusterTimelineItem':
          return ClusterTimelineItem.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ClusterTimelinePage':
          return ClusterTimelinePage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CollabAttachment':
          return CollabAttachment.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CollabAttachmentImageMeta':
          return CollabAttachmentImageMeta.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CollabAttachmentLinks':
          return CollabAttachmentLinks.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CollectionGap':
          return CollectionGap.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CollectionGapPage':
          return CollectionGapPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CollectionGapRecommendationsInner':
          return CollectionGapRecommendationsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CollectorSettings':
          return CollectorSettings.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CollectorStatus':
          return CollectorStatus.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CommentEvent':
          return CommentEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CommonDependencies':
          return CommonDependencies.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CommonDependenciesDependenciesInner':
          return CommonDependenciesDependenciesInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CommonDependenciesDependenciesInnerPathsInner':
          return CommonDependenciesDependenciesInnerPathsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CommonDependenciesRequest':
          return CommonDependenciesRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CompactMessage':
          return CompactMessage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Condition':
          return Condition.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ConfidenceLevel':
          
          
        case 'ContextLink':
          return ContextLink.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ContextLinkRequest':
          return ContextLinkRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ContextPatchEvent':
          return ContextPatchEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ContextSummary':
          return ContextSummary.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ContextSummaryActivityInner':
          return ContextSummaryActivityInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ContextSummaryInsightsInner':
          return ContextSummaryInsightsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ContextSummaryLayerCountsInner':
          return ContextSummaryLayerCountsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ContextSummaryNarrative':
          return ContextSummaryNarrative.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ContextSummaryRequest':
          return ContextSummaryRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Contract':
          return Contract.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CoordinationDetectedEvent':
          return CoordinationDetectedEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CoordinationIndicator':
          return CoordinationIndicator.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CoordinationIndicators':
          return CoordinationIndicators.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CorrelationRecipe':
          return CorrelationRecipe.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CorrelationRecipeCreate':
          return CorrelationRecipeCreate.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CorrelationRecipeDefinition':
          return CorrelationRecipeDefinition.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CorrelationRecipeDefinitionActivateAt':
          return CorrelationRecipeDefinitionActivateAt.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CorrelationRecipeDefinitionContradictionsInner':
          return CorrelationRecipeDefinitionContradictionsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CorrelationRecipeDefinitionDedup':
          return CorrelationRecipeDefinitionDedup.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CorrelationRecipeDefinitionJoinsInner':
          return CorrelationRecipeDefinitionJoinsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CorrelationRecipeDefinitionJoinsInnerWindow':
          return CorrelationRecipeDefinitionJoinsInnerWindow.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CorrelationRecipeDefinitionSeed':
          return CorrelationRecipeDefinitionSeed.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CorrelationRecipeUpdate':
          return CorrelationRecipeUpdate.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CorroborationPayload':
          return CorroborationPayload.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CorsSettings':
          return CorsSettings.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CotBackfillRequest':
          return CotBackfillRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CountResult':
          return CountResult.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CountWindow':
          return CountWindow.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CountryRef':
          return CountryRef.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Coverage':
          return Coverage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CoverageJson':
          return CoverageJson.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CoverageStats':
          return CoverageStats.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CoverageStatsArticles':
          return CoverageStatsArticles.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CoverageStatsCoverageBalance':
          return CoverageStatsCoverageBalance.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CpaBatchRequest':
          return CpaBatchRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CpaBatchRequestFilter':
          return CpaBatchRequestFilter.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CpaBatchResult':
          return CpaBatchResult.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CpaPayload':
          return CpaPayload.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CpaResult':
          return CpaResult.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CredentialField':
          return CredentialField.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CredentialsUpdate':
          return CredentialsUpdate.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CustomSource':
          return CustomSource.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CustomSourceCreated':
          return CustomSourceCreated.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CustomSourceFormats':
          return CustomSourceFormats.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CustomSourceFormatsAlertmanager':
          return CustomSourceFormatsAlertmanager.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CustomSourceFormatsCap':
          return CustomSourceFormatsCap.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CustomSourceFormatsCloudevents':
          return CustomSourceFormatsCloudevents.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CustomSourceFormatsGpslogger':
          return CustomSourceFormatsGpslogger.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CustomSourceFormatsOsmand':
          return CustomSourceFormatsOsmand.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CustomSourceFormatsOverland':
          return CustomSourceFormatsOverland.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CustomSourceFormatsOwntracks':
          return CustomSourceFormatsOwntracks.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CustomSourceInput':
          return CustomSourceInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CyclePacket':
          return CyclePacket.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CyclePacketCoverage':
          return CyclePacketCoverage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CyclePacketForecasts':
          return CyclePacketForecasts.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CyclePacketHypotheses':
          return CyclePacketHypotheses.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CyclePacketItem':
          return CyclePacketItem.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CyclePacketOutlooks':
          return CyclePacketOutlooks.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CyclePacketSituations':
          return CyclePacketSituations.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CycleRun':
          return CycleRun.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CycleRunFailedEvent':
          return CycleRunFailedEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CycleRunPage':
          return CycleRunPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CycleRunRolesInner':
          return CycleRunRolesInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CycleRunRolesInnerPromptTemplate':
          return CycleRunRolesInnerPromptTemplate.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'CycleScope':
          return CycleScope.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'DarkVesselCandidatePayload':
          return DarkVesselCandidatePayload.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'DcpObservationPayload':
          return DcpObservationPayload.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'DeceptionPayload':
          return DeceptionPayload.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'DeceptionPayloadAllOfChecks':
          return DeceptionPayloadAllOfChecks.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'DeclarativePushPayload':
          return DeclarativePushPayload.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'DeclarativePushPayloadNotification':
          return DeclarativePushPayloadNotification.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'DeclaredObservability':
          return DeclaredObservability.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'DeclaredObservabilityKindsValue':
          return DeclaredObservabilityKindsValue.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'DeclaredObservabilityKindsValueArea':
          return DeclaredObservabilityKindsValueArea.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'DeclaredObservabilityKindsValueThresholdsInner':
          return DeclaredObservabilityKindsValueThresholdsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'DerivedEvidenceRef':
          return DerivedEvidenceRef.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'DerivedPayloadCommon':
          return DerivedPayloadCommon.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'DerivedPayloadCommonDetector':
          return DerivedPayloadCommonDetector.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Detectability':
          return Detectability.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'DetectorRef':
          return DetectorRef.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'DeviceInfo':
          return DeviceInfo.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Digest':
          return Digest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'DigestActive':
          return DigestActive.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'DigestAlerts':
          return DigestAlerts.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'DigestAlertsByRuleInner':
          return DigestAlertsByRuleInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'DigestAlertsBySeverity':
          return DigestAlertsBySeverity.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'DigestAlertsByStatus':
          return DigestAlertsByStatus.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'DigestAllOfNext':
          return DigestAllOfNext.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'DigestCount':
          return DigestCount.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'DigestCountEntities':
          return DigestCountEntities.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'DigestCountPrevious':
          return DigestCountPrevious.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'DigestHealthItem':
          return DigestHealthItem.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'DigestNotableItem':
          return DigestNotableItem.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'DigestStats':
          return DigestStats.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'DigestStatsNotableInner':
          return DigestStatsNotableInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Discoverability':
          return Discoverability.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'DoctorFinding':
          return DoctorFinding.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'DoctorOptions':
          return DoctorOptions.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'DoctorReport':
          return DoctorReport.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'DoctorReportFixture':
          return DoctorReportFixture.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'DoctorReportLlm':
          return DoctorReportLlm.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'DoctorReportLlmTokens':
          return DoctorReportLlmTokens.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'DoctorReportPage':
          return DoctorReportPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'DoctorReportProbe':
          return DoctorReportProbe.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'DoctorReportSummary':
          return DoctorReportSummary.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'DoctorReportSummaryTopFinding':
          return DoctorReportSummaryTopFinding.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'DoctorRunAccepted':
          return DoctorRunAccepted.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Dossier':
          return Dossier.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'DossierCoverage':
          return DossierCoverage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'DossierSections':
          return DossierSections.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'DossierSectionsAnalysis':
          return DossierSectionsAnalysis.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'DossierSectionsForecast':
          return DossierSectionsForecast.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'DossierSectionsForecastOfficialInner':
          return DossierSectionsForecastOfficialInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'DossierSectionsImagery':
          return DossierSectionsImagery.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'DossierSectionsImageryItemsInner':
          return DossierSectionsImageryItemsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'DossierSectionsImpacts':
          return DossierSectionsImpacts.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'DossierSectionsImpactsItemsInner':
          return DossierSectionsImpactsItemsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'DossierSectionsOverview':
          return DossierSectionsOverview.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'DossierSectionsOverviewFacets':
          return DossierSectionsOverviewFacets.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'DossierSectionsOverviewLinks':
          return DossierSectionsOverviewLinks.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'DossierSectionsRelated':
          return DossierSectionsRelated.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'DossierSectionsTimeline':
          return DossierSectionsTimeline.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'DossierSectionsTimelineItemsInner':
          return DossierSectionsTimelineItemsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EdpAnalysisRequest':
          return EdpAnalysisRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EdpBatch':
          return EdpBatch.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EdpCoverage':
          return EdpCoverage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EdpDeleteItem':
          return EdpDeleteItem.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EdpDocumentItem':
          return EdpDocumentItem.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EdpDocumentItemAuthor':
          return EdpDocumentItemAuthor.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EdpDocumentItemMediaInner':
          return EdpDocumentItemMediaInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EdpEventItem':
          return EdpEventItem.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EdpFeatureItem':
          return EdpFeatureItem.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EdpForecastItem':
          return EdpForecastItem.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EdpForecastItemAllOfQuestion':
          return EdpForecastItemAllOfQuestion.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EdpGeometry':
          return EdpGeometry.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EdpIngestResult':
          return EdpIngestResult.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EdpItem':
          return EdpItem.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EdpItemRejection':
          return EdpItemRejection.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EdpItemRejectionSchemaErrorsInner':
          return EdpItemRejectionSchemaErrorsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EdpManifest':
          return EdpManifest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EdpObservationItem':
          return EdpObservationItem.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EdpOrigin':
          return EdpOrigin.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EdpPresentation':
          return EdpPresentation.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EdpProtocolInfo':
          return EdpProtocolInfo.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EdpPull':
          return EdpPull.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EdpSearch':
          return EdpSearch.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EdpSnapshot':
          return EdpSnapshot.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EdpSourceInfo':
          return EdpSourceInfo.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EdpSourceInfoAccess':
          return EdpSourceInfoAccess.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EdpSourceInfoLicence':
          return EdpSourceInfoLicence.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EdpSourceInfoOriginator':
          return EdpSourceInfoOriginator.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EdpSourceInfoPublisher':
          return EdpSourceInfoPublisher.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EdpSourceInfoTerms':
          return EdpSourceInfoTerms.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EdpStatusItem':
          return EdpStatusItem.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EdpStream':
          return EdpStream.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EdpStreamFields':
          return EdpStreamFields.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EdpSuggestedBinding':
          return EdpSuggestedBinding.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EdpTransport':
          return EdpTransport.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EdpValidationReport':
          return EdpValidationReport.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EdpValidationRequest':
          return EdpValidationRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EdrInstanceList':
          return EdrInstanceList.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EdrInstanceListInstancesInner':
          return EdrInstanceListInstancesInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EmergencySquawkPayload':
          return EmergencySquawkPayload.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EmwinProductPayload':
          return EmwinProductPayload.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EmwinWarningPayload':
          return EmwinWarningPayload.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EmwinWarningPayloadDuplicateOf':
          return EmwinWarningPayloadDuplicateOf.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EnableCoreSourcesResult':
          return EnableCoreSourcesResult.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EnableFreeSourcesResult':
          return EnableFreeSourcesResult.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EnsembleRefitResult':
          return EnsembleRefitResult.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EnsembleRefitResultExtremizing':
          return EnsembleRefitResultExtremizing.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EnsembleRefitResultPlattInner':
          return EnsembleRefitResultPlattInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EnsembleRefitResultWeightsInner':
          return EnsembleRefitResultWeightsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Entity':
          return Entity.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EntityAlias':
          return EntityAlias.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EntityKey':
          return EntityKey.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EntityKeyInput':
          return EntityKeyInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EntityLink':
          return EntityLink.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EntityMergeInput':
          return EntityMergeInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EntityPage':
          return EntityPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EntityRecord':
          return EntityRecord.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EntityRecordList':
          return EntityRecordList.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Escalation':
          return Escalation.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EscalationPolicy':
          return EscalationPolicy.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EscalationPolicyInput':
          return EscalationPolicyInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EscalationPolicyInputRepeat':
          return EscalationPolicyInputRepeat.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EscalationPolicyInputStopOn':
          return EscalationPolicyInputStopOn.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EscalationStep':
          return EscalationStep.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EtaResult':
          return EtaResult.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EtaResultTarget':
          return EtaResultTarget.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EventSelector':
          return EventSelector.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EventSelectorChangePoint':
          return EventSelectorChangePoint.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EventSelectorSituationActivated':
          return EventSelectorSituationActivated.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EventSelectorUnexplainedMove':
          return EventSelectorUnexplainedMove.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EventStreamAlertsFilter':
          return EventStreamAlertsFilter.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EventStreamChangesFilter':
          return EventStreamChangesFilter.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EventStreamFilter':
          return EventStreamFilter.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EventStreamFilterAlerts':
          return EventStreamFilterAlerts.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EventStreamFilterChronicle':
          return EventStreamFilterChronicle.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EventStreamFilterExternal':
          return EventStreamFilterExternal.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EventStreamFilterWorkspace':
          return EventStreamFilterWorkspace.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EventStudyRequest':
          return EventStudyRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EventStudyResult':
          return EventStudyResult.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EventStudyResultCaar':
          return EventStudyResultCaar.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EventStudyResultEventsInner':
          return EventStudyResultEventsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EventStudyResultEventsInnerAbnormalInner':
          return EventStudyResultEventsInnerAbnormalInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EventStudyResultEventsInnerCar':
          return EventStudyResultEventsInnerCar.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EventStudyResultWindows':
          return EventStudyResultWindows.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EventStudyWindowStats':
          return EventStudyWindowStats.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EventStudyWindowStatsPlacebo':
          return EventStudyWindowStatsPlacebo.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EvidenceEvent':
          return EvidenceEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EvidenceGroup':
          return EvidenceGroup.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EvidenceItem':
          return EvidenceItem.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EvidenceItemInput':
          return EvidenceItemInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EvidenceItemPage':
          return EvidenceItemPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'EvidenceItemPatch':
          return EvidenceItemPatch.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ExchangePackage':
          return ExchangePackage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ExchangePackageDeliveriesInner':
          return ExchangePackageDeliveriesInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ExchangePackageInput':
          return ExchangePackageInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ExchangePackageInputExtract':
          return ExchangePackageInputExtract.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ExchangePackagePage':
          return ExchangePackagePage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ExchangeReceipt':
          return ExchangeReceipt.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ExerciseRun':
          return ExerciseRun.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ExerciseRunCreate':
          return ExerciseRunCreate.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ExerciseRunPage':
          return ExerciseRunPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ExerciseStats':
          return ExerciseStats.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ExerciseStatsSkippedRulesInner':
          return ExerciseStatsSkippedRulesInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ExportFile':
          return ExportFile.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ExportJob':
          return ExportJob.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ExportJobPage':
          return ExportJobPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ExportLicenceSummary':
          return ExportLicenceSummary.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ExportLicenceSummaryExcludedInner':
          return ExportLicenceSummaryExcludedInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ExportLicenceSummaryLicencesInner':
          return ExportLicenceSummaryLicencesInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ExportManifest':
          return ExportManifest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ExportManifestFilesInner':
          return ExportManifestFilesInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ExportManifestSignature':
          return ExportManifestSignature.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ExportSpec':
          return ExportSpec.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Exposure':
          return Exposure.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ExposureArea':
          return ExposureArea.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ExposureDependents':
          return ExposureDependents.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ExposureDependentsByLabel':
          return ExposureDependentsByLabel.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ExposureFacilities':
          return ExposureFacilities.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ExposurePlaces':
          return ExposurePlaces.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ExposurePlacesItemsInner':
          return ExposurePlacesItemsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ExposurePopulation':
          return ExposurePopulation.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ExposureRequest':
          return ExposureRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ExposureTraffic':
          return ExposureTraffic.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ExposureWatchlistEntities':
          return ExposureWatchlistEntities.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ExposureWatchlistEntitiesCountsInner':
          return ExposureWatchlistEntitiesCountsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ExternalBinding':
          return ExternalBinding.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ExternalBindingEvent':
          return ExternalBindingEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ExternalBindingInput':
          return ExternalBindingInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ExternalBindingPreview':
          return ExternalBindingPreview.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ExternalBindingPreviewEffectsInner':
          return ExternalBindingPreviewEffectsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ExternalCatalogueEntry':
          return ExternalCatalogueEntry.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ExternalCatalogueEntryTimeDimension':
          return ExternalCatalogueEntryTimeDimension.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ExternalForecasterRegistration':
          return ExternalForecasterRegistration.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ExternalQuestionLink':
          return ExternalQuestionLink.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ExternalQuestionLinkInput':
          return ExternalQuestionLinkInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ExternalSource':
          return ExternalSource.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ExternalSourceCreated':
          return ExternalSourceCreated.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ExternalSourcePage':
          return ExternalSourcePage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ExternalSourcePull':
          return ExternalSourcePull.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ExternalSourceRegistration':
          return ExternalSourceRegistration.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ExternalSourceStateEvent':
          return ExternalSourceStateEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ExternalStreamStats':
          return ExternalStreamStats.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'FcmProviderConfig':
          return FcmProviderConfig.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Feature':
          return Feature.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'FeatureCollection':
          return FeatureCollection.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'FeatureProperties':
          return FeatureProperties.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'FederationChangePage':
          return FederationChangePage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'FederationInfo':
          return FederationInfo.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'FederationIngestBatch':
          return FederationIngestBatch.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'FederationIngestResult':
          return FederationIngestResult.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'FederationIngestResultDropped':
          return FederationIngestResultDropped.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'FederationItem':
          return FederationItem.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'FederationSnapshotPage':
          return FederationSnapshotPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'FeedKey':
          return FeedKey.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'FeedKeyCreated':
          return FeedKeyCreated.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'FeedKeyCreatedAllOfUrls':
          return FeedKeyCreatedAllOfUrls.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'FeedKeyInput':
          return FeedKeyInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'FeltReportEstimatePayload':
          return FeltReportEstimatePayload.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'FetchLogEntry':
          return FetchLogEntry.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'FetchOutcome':
          return FetchOutcome.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'FetchPage':
          return FetchPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'FieldEncodings':
          return FieldEncodings.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'FieldEncodingsI16':
          return FieldEncodingsI16.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'FieldEncodingsPngTiles':
          return FieldEncodingsPngTiles.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'FieldEncodingsPngUv':
          return FieldEncodingsPngUv.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'FieldEncodingsVelocityJson':
          return FieldEncodingsVelocityJson.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'FieldManifest':
          return FieldManifest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'FieldManifestComponentsInner':
          return FieldManifestComponentsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'FieldManifestCycle':
          return FieldManifestCycle.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'FieldManifestGrid':
          return FieldManifestGrid.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'FieldSpec':
          return FieldSpec.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'FieldThreshold':
          return FieldThreshold.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'FilterAppliedEvent':
          return FilterAppliedEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'FindingBaseRate':
          return FindingBaseRate.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'FindingEvent':
          return FindingEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'FindingEventFinding':
          return FindingEventFinding.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'FindingPage':
          return FindingPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'FindingPatch':
          return FindingPatch.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'FindingSeverity':
          
          
        case 'FireClusterPayload':
          return FireClusterPayload.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'FireDetectionPayload':
          return FireDetectionPayload.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Forecast':
          return Forecast.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ForecastAnnulInput':
          return ForecastAnnulInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ForecastAreaPayload':
          return ForecastAreaPayload.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ForecastAreaPayloadThreshold':
          return ForecastAreaPayloadThreshold.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ForecastCalibration':
          return ForecastCalibration.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ForecastCalibrationBinsInner':
          return ForecastCalibrationBinsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ForecastCalibrationCurveInner':
          return ForecastCalibrationCurveInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ForecastCalibrationDecomposition':
          return ForecastCalibrationDecomposition.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ForecastCitationsInner':
          return ForecastCitationsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ForecastEvaluation':
          return ForecastEvaluation.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ForecastEvaluationRequest':
          return ForecastEvaluationRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ForecastEvaluationSkipped':
          return ForecastEvaluationSkipped.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ForecastHistory':
          return ForecastHistory.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ForecastHistorySeriesInner':
          return ForecastHistorySeriesInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ForecastHistorySeriesInnerPointsInner':
          return ForecastHistorySeriesInnerPointsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ForecastInput':
          return ForecastInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ForecastNumericCalibration':
          return ForecastNumericCalibration.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ForecastNumericCalibrationPit':
          return ForecastNumericCalibrationPit.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ForecastNumericCalibrationPitBinsInner':
          return ForecastNumericCalibrationPitBinsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ForecastNumericCalibrationQuantileReliabilityInner':
          return ForecastNumericCalibrationQuantileReliabilityInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ForecastNumericCalibrationSummary':
          return ForecastNumericCalibrationSummary.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ForecastPage':
          return ForecastPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ForecastQuestion':
          return ForecastQuestion.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ForecastQuestionEvent':
          return ForecastQuestionEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ForecastQuestionInput':
          return ForecastQuestionInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ForecastQuestionPage':
          return ForecastQuestionPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ForecastQuestionPatch':
          return ForecastQuestionPatch.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ForecastQuestionSummary':
          return ForecastQuestionSummary.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ForecastResolution':
          return ForecastResolution.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ForecastResolutionInput':
          return ForecastResolutionInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ForecastResolver':
          return ForecastResolver.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ForecastScore':
          return ForecastScore.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ForecastScoreIntervalHits':
          return ForecastScoreIntervalHits.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ForecastScorecard':
          return ForecastScorecard.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ForecastSeries':
          return ForecastSeries.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ForecastSeriesPoint':
          return ForecastSeriesPoint.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ForecastSubmittedEvent':
          return ForecastSubmittedEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Forecaster':
          return Forecaster.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ForecasterComparison':
          return ForecasterComparison.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ForecasterPage':
          return ForecasterPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ForecasterTrackRecord':
          return ForecasterTrackRecord.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ForecasterUpdate':
          return ForecasterUpdate.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'FrameItem':
          return FrameItem.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'FrameStats':
          return FrameStats.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'GeofenceRef':
          return GeofenceRef.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Geometry':
          return Geometry.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'GetForecastCalibration200Response':
          return GetForecastCalibration200Response.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'GetIntelCalibration200Response':
          return GetIntelCalibration200Response.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'GetPrincipal200Response':
          return GetPrincipal200Response.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'GetVapidPublicKey200Response':
          return GetVapidPublicKey200Response.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Glance':
          return Glance.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'GlanceActivity':
          return GlanceActivity.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'GlanceAlerts':
          return GlanceAlerts.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'GlanceAlertsLatestInner':
          return GlanceAlertsLatestInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'GlanceAlertsOpen':
          return GlanceAlertsOpen.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'GlanceBriefing':
          return GlanceBriefing.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'GlanceSources':
          return GlanceSources.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'GotifyMessage':
          return GotifyMessage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'GotifyMessageInput':
          return GotifyMessageInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'GpsLoggerFix':
          return GpsLoggerFix.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'GraphRequest':
          return GraphRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'GraphResult':
          return GraphResult.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'GroundStationPayload':
          return GroundStationPayload.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Group':
          return Group.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'GroupInput':
          return GroupInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'GroupMember':
          return GroupMember.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'GroupMemberInput':
          return GroupMemberInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'GroupMembersInput':
          return GroupMembersInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'GroupPage':
          return GroupPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'GroupPatch':
          return GroupPatch.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'HazardForecast':
          return HazardForecast.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'HazardForecastProbabilityFieldInner':
          return HazardForecastProbabilityFieldInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'HazardForecastProbabilityRange':
          return HazardForecastProbabilityRange.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'HealthStatus':
          
          
        case 'HelloEvent':
          return HelloEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'HfPropagationAnomalyPayload':
          return HfPropagationAnomalyPayload.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'HistoryFrame':
          return HistoryFrame.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'HistoryFrameRemovalsInner':
          return HistoryFrameRemovalsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'HistoryFrames':
          return HistoryFrames.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'HistoryPage':
          return HistoryPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'HistoryTier':
          
          
        case 'Hold':
          return Hold.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'HoldInput':
          return HoldInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'HoldPage':
          return HoldPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'HoldVersionPage':
          return HoldVersionPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'HoldingPatternPayload':
          return HoldingPatternPayload.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'HoldingPatternPayloadAllOfNearestAirport':
          return HoldingPatternPayloadAllOfNearestAirport.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'HoldoverFireRiskPayload':
          return HoldoverFireRiskPayload.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Hypothesis':
          return Hypothesis.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'HypothesisCapOverride':
          return HypothesisCapOverride.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'HypothesisChange':
          return HypothesisChange.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'HypothesisCoincidenceBaseline':
          return HypothesisCoincidenceBaseline.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'HypothesisConfidenceBasis':
          return HypothesisConfidenceBasis.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'HypothesisInput':
          return HypothesisInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'HypothesisInputPriorBasis':
          return HypothesisInputPriorBasis.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'HypothesisPatch':
          return HypothesisPatch.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'HypothesisPatchProbabilityOverride':
          return HypothesisPatchProbabilityOverride.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'HypothesisPriorBasis':
          return HypothesisPriorBasis.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'HypothesisProbabilityState':
          return HypothesisProbabilityState.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'HypothesisReviewInput':
          return HypothesisReviewInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'HypothesisReviewInputAssumptionsInner':
          return HypothesisReviewInputAssumptionsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'HypothesisReviewInputLrInner':
          return HypothesisReviewInputLrInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'HypothesisRevision':
          return HypothesisRevision.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'HypothesisRevisionPage':
          return HypothesisRevisionPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'HypothesisSet':
          return HypothesisSet.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'HypothesisSetDraft':
          return HypothesisSetDraft.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'HypothesisSetDraftRequest':
          return HypothesisSetDraftRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'HypothesisSetDraftUsage':
          return HypothesisSetDraftUsage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'HypothesisSetDraftWarningsInner':
          return HypothesisSetDraftWarningsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'HypothesisSetEvent':
          return HypothesisSetEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'HypothesisSetEventBandChangesInner':
          return HypothesisSetEventBandChangesInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'HypothesisSetInput':
          return HypothesisSetInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'HypothesisSetInputLedgerQuestion':
          return HypothesisSetInputLedgerQuestion.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'HypothesisSetInputLedgerQuestionResolver':
          return HypothesisSetInputLedgerQuestionResolver.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'HypothesisSetPage':
          return HypothesisSetPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'HypothesisSetPatch':
          return HypothesisSetPatch.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IcdTerm':
          
          
        case 'ImpactProjectionEvent':
          return ImpactProjectionEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ImpactRate':
          return ImpactRate.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ImpactRateInterval':
          return ImpactRateInterval.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ImpactRatePage':
          return ImpactRatePage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ImportMediaRatingsRequest1':
          return ImportMediaRatingsRequest1.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ImportMediaRatingsRequest1RowsInner':
          return ImportMediaRatingsRequest1RowsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ImportReport':
          return ImportReport.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'InboxItem':
          return InboxItem.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'InboxPage':
          return InboxPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IndexChangedEvent':
          return IndexChangedEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IndexFitResult':
          return IndexFitResult.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IndexFitResultComponentsInner':
          return IndexFitResultComponentsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IndexWeightsInput':
          return IndexWeightsInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IndexWeightsProposedEvent':
          return IndexWeightsProposedEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Indicator':
          return Indicator.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IndicatorEvent':
          return IndicatorEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IndicatorInput':
          return IndicatorInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IndicatorLikelihood':
          return IndicatorLikelihood.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IndicatorPage':
          return IndicatorPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IndicatorPatch':
          return IndicatorPatch.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IndicatorSignal':
          return IndicatorSignal.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IndicatorSignalIntelEvent':
          return IndicatorSignalIntelEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IndicatorSignalQuestion':
          return IndicatorSignalQuestion.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IndicatorSignalSeries':
          return IndicatorSignalSeries.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IndicatorSignalSeriesCondition':
          return IndicatorSignalSeriesCondition.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IndicatorSignalSituation':
          return IndicatorSignalSituation.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'InformationValue':
          return InformationValue.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'InformationValueIndicatorsInner':
          return InformationValueIndicatorsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'InfraBuild':
          return InfraBuild.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'InfraEdge':
          return InfraEdge.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'InfraEdgeEvidence':
          return InfraEdgeEvidence.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'InfraEdgePage':
          return InfraEdgePage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'InfraImpactRequest':
          return InfraImpactRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'InfraImpactRequestRecord':
          return InfraImpactRequestRecord.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'InfraImpactResult':
          return InfraImpactResult.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'InfraImpactResultDependentsInner':
          return InfraImpactResultDependentsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'InfraImpactResultDependentsInnerPathInner':
          return InfraImpactResultDependentsInnerPathInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'InfraImpactResultSummary':
          return InfraImpactResultSummary.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'InfraNode':
          return InfraNode.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'InfraNodeEdges':
          return InfraNodeEdges.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'InfraNodePage':
          return InfraNodePage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'InfraRules':
          return InfraRules.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'InfraRulesRulesInner':
          return InfraRulesRulesInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'InfraStatus':
          return InfraStatus.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IngestDevice':
          return IngestDevice.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IngestDeviceCreated':
          return IngestDeviceCreated.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IngestDeviceInput':
          return IngestDeviceInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IngestDeviceSetup':
          return IngestDeviceSetup.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IngestDeviceSetupGpslogger':
          return IngestDeviceSetupGpslogger.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IngestDeviceSetupOverland':
          return IngestDeviceSetupOverland.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IngestDeviceSetupTraccar':
          return IngestDeviceSetupTraccar.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IngestFormatInfo':
          return IngestFormatInfo.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IngestFormatOption':
          return IngestFormatOption.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IngestKeyResponse':
          return IngestKeyResponse.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IngestOutcome':
          return IngestOutcome.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IngestOutcomeRejectionsInner':
          return IngestOutcomeRejectionsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IngestPolicy':
          return IngestPolicy.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IngestPolicyAll':
          return IngestPolicyAll.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IngestPolicyLatest':
          return IngestPolicyLatest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IngestPolicySampled':
          return IngestPolicySampled.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IngestRecords':
          return IngestRecords.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IngestRecordsFormatRequest':
          return IngestRecordsFormatRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IngestRecordsPathKey200Response':
          return IngestRecordsPathKey200Response.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IngestRecordsPathKeyRequest':
          return IngestRecordsPathKeyRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IngestRecordsRequest':
          return IngestRecordsRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IntelBoard':
          return IntelBoard.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IntelBoardBandChangesInner':
          return IntelBoardBandChangesInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IntelBoardBenchmarksInner':
          return IntelBoardBenchmarksInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IntelBoardCalibration':
          return IntelBoardCalibration.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IntelBoardCalibrationEnsemble':
          return IntelBoardCalibrationEnsemble.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IntelBoardCalibrationOperator':
          return IntelBoardCalibrationOperator.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IntelBoardForecastsDueInner':
          return IntelBoardForecastsDueInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IntelBoardIndicatorsObservedInner':
          return IntelBoardIndicatorsObservedInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IntelBoardLatestAssessmentsInner':
          return IntelBoardLatestAssessmentsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IntelBoardSituationsInner':
          return IntelBoardSituationsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IntelCalibration':
          return IntelCalibration.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IntelCalibrationBinsInner':
          return IntelCalibrationBinsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IntelCalibrationCurveInner':
          return IntelCalibrationCurveInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IntelCorroboration':
          return IntelCorroboration.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IntelCorroborationAutoCorroborate':
          return IntelCorroborationAutoCorroborate.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IntelCorroborationEventQuality':
          return IntelCorroborationEventQuality.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IntelCorroborationSituationActivation':
          return IntelCorroborationSituationActivation.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IntelEventAlternative':
          return IntelEventAlternative.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IntelEventInput':
          return IntelEventInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IntelEventLogEntry':
          return IntelEventLogEntry.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IntelEventMember':
          return IntelEventMember.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IntelEventMemberInput':
          return IntelEventMemberInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IntelEventMemberJoinReason':
          return IntelEventMemberJoinReason.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IntelEventMergeInput':
          return IntelEventMergeInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IntelEventPage':
          return IntelEventPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IntelEventPatch':
          return IntelEventPatch.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IntelEventRecordPayload':
          return IntelEventRecordPayload.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IntelEventStreamEvent':
          return IntelEventStreamEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IntelEventSummary':
          return IntelEventSummary.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IntelligenceEvent':
          return IntelligenceEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IntelligenceEventContext':
          return IntelligenceEventContext.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IntelligenceEventEvidence':
          return IntelligenceEventEvidence.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IntelligenceEventInference':
          return IntelligenceEventInference.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'IntelligenceEventSpeculation':
          return IntelligenceEventSpeculation.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'InteractiveChatConfig':
          return InteractiveChatConfig.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Investigation':
          return Investigation.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'InvestigationExport':
          return InvestigationExport.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'InvestigationInput':
          return InvestigationInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'InvestigationItem':
          return InvestigationItem.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'InvestigationItemInput':
          return InvestigationItemInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'InvestigationPage':
          return InvestigationPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'InvestigationPatch':
          return InvestigationPatch.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'JammingCellPayload':
          return JammingCellPayload.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'JsonFeed':
          return JsonFeed.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'JsonFeedAuthorsInner':
          return JsonFeedAuthorsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'JsonFeedItem':
          return JsonFeedItem.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'JsonFeedItemAuthorsInner':
          return JsonFeedItemAuthorsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'JsonFeedItemForesignal':
          return JsonFeedItemForesignal.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Jwk':
          return Jwk.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Jwks':
          return Jwks.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'KeyAssumption':
          return KeyAssumption.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'KeyAssumptionEvent':
          return KeyAssumptionEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'KeyAssumptionInput':
          return KeyAssumptionInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'KeyAssumptionPatch':
          return KeyAssumptionPatch.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'KeyJudgment':
          return KeyJudgment.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'KeyJudgmentChange':
          return KeyJudgmentChange.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'KindDisplay':
          return KindDisplay.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'KindSchema':
          return KindSchema.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'KindSummary':
          return KindSummary.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'KinematicState':
          return KinematicState.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'LeadTimeStats':
          return LeadTimeStats.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'LeadTimeStatsFilters':
          return LeadTimeStatsFilters.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'LeadTimeStatsGroupsInner':
          return LeadTimeStatsGroupsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'LeadTimeSummary':
          return LeadTimeSummary.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'LedgerQuestionBrief':
          return LedgerQuestionBrief.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'LedgerQuestionBriefForecastersInner':
          return LedgerQuestionBriefForecastersInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Legend':
          return Legend.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'LegendTick':
          return LegendTick.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'LicenceTerms':
          return LicenceTerms.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'LightningClusterPayload':
          return LightningClusterPayload.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ListRecords200Response':
          return ListRecords200Response.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'LiveEvent':
          return LiveEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'LlmModelInfo':
          return LlmModelInfo.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'LlmProvider':
          return LlmProvider.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'LlmProviderInput':
          return LlmProviderInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'LlmProviderInputOptions':
          return LlmProviderInputOptions.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'LlmSettings':
          return LlmSettings.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'LlmTestResult':
          return LlmTestResult.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'LlmTestResultChat':
          return LlmTestResultChat.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'LlmTestResultEmbedding':
          return LlmTestResultEmbedding.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'LlmUsagePoint':
          return LlmUsagePoint.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'LlmUsageSeries':
          return LlmUsageSeries.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'LoiterPayload':
          return LoiterPayload.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MaintenanceRun':
          return MaintenanceRun.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MaintenanceRunStepsInner':
          return MaintenanceRunStepsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MapAttachmentConfig':
          return MapAttachmentConfig.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MapSpriteIndexValue':
          return MapSpriteIndexValue.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MapStyle':
          return MapStyle.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MapStyleMetadata':
          return MapStyleMetadata.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MapStyleMetadataLayersInner':
          return MapStyleMetadataLayersInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MappingField':
          return MappingField.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MappingFieldUnit':
          return MappingFieldUnit.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MappingPreset':
          return MappingPreset.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MappingPresetSetup':
          return MappingPresetSetup.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MappingPreviewRequest':
          return MappingPreviewRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MappingPreviewResult':
          return MappingPreviewResult.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MappingRejection':
          return MappingRejection.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MappingSample':
          return MappingSample.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MappingSuggestRequest':
          return MappingSuggestRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MappingSuggestRequestHints':
          return MappingSuggestRequestHints.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MappingSuggestResult':
          return MappingSuggestResult.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MappingTestRequest':
          return MappingTestRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MappingTestResult':
          return MappingTestResult.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MappingTestResultSamplesInner':
          return MappingTestResultSamplesInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MappingTimeSpec':
          return MappingTimeSpec.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MappingTimeSpecOneOf':
          return MappingTimeSpecOneOf.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MappingTimeSpecOneOf1':
          return MappingTimeSpecOneOf1.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MappingValueSpec':
          return MappingValueSpec.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MappingValueSpecOneOf':
          return MappingValueSpecOneOf.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MappingValueSpecOneOf1':
          return MappingValueSpecOneOf1.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MappingValueSpecOneOf2':
          return MappingValueSpecOneOf2.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MappingWarning':
          return MappingWarning.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'McpAuthSettings':
          return McpAuthSettings.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Me':
          return Me.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MeGroupsInner':
          return MeGroupsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MeToken':
          return MeToken.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Media':
          return Media.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MediaFingerprint':
          return MediaFingerprint.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MediaFingerprintUsesInner':
          return MediaFingerprintUsesInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MediaLinks':
          return MediaLinks.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MediaMatch':
          return MediaMatch.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MediaMatchRequest':
          return MediaMatchRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MediaMatchResult':
          return MediaMatchResult.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MediaMatchResultQuery':
          return MediaMatchResultQuery.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MediaOutlet':
          return MediaOutlet.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MediaOutletPage':
          return MediaOutletPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MediaOutletPatch':
          return MediaOutletPatch.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MediaProvider':
          return MediaProvider.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MediaProviderInput':
          return MediaProviderInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MediaRating':
          return MediaRating.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MediaReuseEvent':
          return MediaReuseEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MeshChannelCommonConfig':
          return MeshChannelCommonConfig.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MeshChannelCommonConfigAirtimeCustom':
          return MeshChannelCommonConfigAirtimeCustom.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MeshcoreChannelConfig':
          return MeshcoreChannelConfig.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MeshtasticChannelConfig':
          return MeshtasticChannelConfig.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MetarPayload':
          return MetarPayload.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MetarPayloadSkyInner':
          return MetarPayloadSkyInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MetricProjection':
          return MetricProjection.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MetricProjectionLabelsInner':
          return MetricProjectionLabelsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MetricSettings':
          return MetricSettings.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MilNavalCooccurrencePayload':
          return MilNavalCooccurrencePayload.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ModelDisagreement':
          return ModelDisagreement.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MotionSpec':
          return MotionSpec.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MqttChannelConfig':
          return MqttChannelConfig.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'MqttConnections':
          return MqttConnections.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Narrative':
          return Narrative.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NarrativeEvent':
          return NarrativeEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NarrativeLogEntry':
          return NarrativeLogEntry.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NarrativeMergeInput':
          return NarrativeMergeInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NarrativePage':
          return NarrativePage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NarrativePlatformStats':
          return NarrativePlatformStats.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NarrativePost':
          return NarrativePost.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NarrativePostPage':
          return NarrativePostPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NarrativeSplitInput':
          return NarrativeSplitInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NarrativeSummary':
          return NarrativeSummary.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NarrativeTimeline':
          return NarrativeTimeline.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NarrativeTimelineSeriesInner':
          return NarrativeTimelineSeriesInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NarrativeTimelineSeriesInnerPointsInner':
          return NarrativeTimelineSeriesInnerPointsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NativePushPayload':
          return NativePushPayload.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NativePushPayloadApns':
          return NativePushPayloadApns.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NativePushPayloadApnsAps':
          return NativePushPayloadApnsAps.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NativePushPayloadApnsApsAlert':
          return NativePushPayloadApnsApsAlert.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NativePushPayloadApnsE':
          return NativePushPayloadApnsE.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NativePushPayloadFcm':
          return NativePushPayloadFcm.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NativePushPayloadFcmMessage':
          return NativePushPayloadFcmMessage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NativePushPayloadFcmMessageAndroid':
          return NativePushPayloadFcmMessageAndroid.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NativePushPayloadFcmMessageData':
          return NativePushPayloadFcmMessageData.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Near':
          return Near.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NearOneOf':
          return NearOneOf.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NearOneOf1':
          return NearOneOf1.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NearOneOf2':
          return NearOneOf2.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NearOneOf2Anchor':
          return NearOneOf2Anchor.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NetworkSaltInput':
          return NetworkSaltInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NetworkSaltStatus':
          return NetworkSaltStatus.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NewsArticle':
          return NewsArticle.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NewsArticlePage':
          return NewsArticlePage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NewsStory':
          return NewsStory.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NewsStoryOutletsInner':
          return NewsStoryOutletsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NewsStoryPage':
          return NewsStoryPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NormalizedRecordPreview':
          return NormalizedRecordPreview.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NormalizedRecordPreviewGeometry':
          return NormalizedRecordPreviewGeometry.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Notification':
          return Notification.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NotificationAction':
          return NotificationAction.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NotificationActionParams':
          return NotificationActionParams.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NotificationBadge':
          return NotificationBadge.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NotificationDelivery':
          return NotificationDelivery.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NotificationDeliveryPage':
          return NotificationDeliveryPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NotificationDetail':
          return NotificationDetail.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NotificationLink':
          return NotificationLink.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NotificationMessage':
          return NotificationMessage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NotificationPage':
          return NotificationPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NotificationPreference':
          return NotificationPreference.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NotificationPreferenceChannel':
          return NotificationPreferenceChannel.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NotificationPreferenceInput':
          return NotificationPreferenceInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NotificationPreferenceInputChannel':
          return NotificationPreferenceInputChannel.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NotificationPreferencesInput':
          return NotificationPreferencesInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NotificationReadRequest':
          return NotificationReadRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NotificationReadRequestOneOf':
          return NotificationReadRequestOneOf.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NotificationReadRequestOneOf1':
          return NotificationReadRequestOneOf1.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NotificationSettings':
          return NotificationSettings.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NotificationSettingsChatops':
          return NotificationSettingsChatops.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NotificationWebhookMessage':
          return NotificationWebhookMessage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NtfyMessage':
          return NtfyMessage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NtfyPublishRequest':
          return NtfyPublishRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NwsAlertPayload':
          return NwsAlertPayload.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NwsForecastPayload':
          return NwsForecastPayload.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'NwsForecastPayloadHazardsInner':
          return NwsForecastPayloadHazardsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ObjectAcl':
          return ObjectAcl.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ObjectAclEntry':
          return ObjectAclEntry.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ObjectAclEntryInput':
          return ObjectAclEntryInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ObjectAclInput':
          return ObjectAclInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ObjectAction':
          return ObjectAction.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ObjectEnvelope':
          return ObjectEnvelope.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ObjectEnvelopeLinks':
          return ObjectEnvelopeLinks.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ObjectEnvelopePage':
          return ObjectEnvelopePage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ObjectEnvelopeTime':
          return ObjectEnvelopeTime.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ObjectResolveRequest':
          return ObjectResolveRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ObjectResolveResult':
          return ObjectResolveResult.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ObservabilityCoverage':
          return ObservabilityCoverage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ObservabilityEstimate':
          return ObservabilityEstimate.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ObservationLink':
          return ObservationLink.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ObservationLinkLinkReason':
          return ObservationLinkLinkReason.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Observer':
          return Observer.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'OgcCollection':
          return OgcCollection.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'OgcCollection200Response':
          return OgcCollection200Response.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'OgcCollectionExtent':
          return OgcCollectionExtent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'OgcCollectionExtentSpatial':
          return OgcCollectionExtentSpatial.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'OgcCollectionExtentTemporal':
          return OgcCollectionExtentTemporal.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'OgcCollections':
          return OgcCollections.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'OgcConformance':
          return OgcConformance.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'OgcCoverageCollection':
          return OgcCoverageCollection.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'OgcCoverageCollectionDataQueries':
          return OgcCoverageCollectionDataQueries.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'OgcCoverageCollectionDataQueriesArea':
          return OgcCoverageCollectionDataQueriesArea.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'OgcCoverageCollectionDataQueriesInstances':
          return OgcCoverageCollectionDataQueriesInstances.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'OgcCoverageCollectionDataQueriesPosition':
          return OgcCoverageCollectionDataQueriesPosition.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'OgcCoverageCollectionExtent':
          return OgcCoverageCollectionExtent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'OgcCoverageCollectionExtentSpatial':
          return OgcCoverageCollectionExtentSpatial.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'OgcCoverageCollectionExtentTemporal':
          return OgcCoverageCollectionExtentTemporal.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'OgcFeature':
          return OgcFeature.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'OgcFeatureCollection':
          return OgcFeatureCollection.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'OgcFeatureProperties':
          return OgcFeatureProperties.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'OgcLandingPage':
          return OgcLandingPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'OgcLink':
          return OgcLink.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'OgcQueryables':
          return OgcQueryables.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'OgcTileMatrixSet':
          return OgcTileMatrixSet.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'OgcTileMatrixSetList':
          return OgcTileMatrixSetList.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'OgcTileMatrixSetListTileMatrixSetsInner':
          return OgcTileMatrixSetListTileMatrixSetsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'OgcTileMatrixSetTileMatricesInner':
          return OgcTileMatrixSetTileMatricesInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'OgcTileSet':
          return OgcTileSet.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'OgcTileSetList':
          return OgcTileSetList.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'OgcTileSetListTilesetsInner':
          return OgcTileSetListTilesetsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'OgcTileSetTileMatrixSetLimitsInner':
          return OgcTileSetTileMatrixSetLimitsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'OpaqueChronicleLink':
          return OpaqueChronicleLink.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Originator':
          return Originator.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'OsmAndLocation':
          return OsmAndLocation.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'OsmAndLocationLocation':
          return OsmAndLocationLocation.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'OsmAndLocationLocationBattery':
          return OsmAndLocationLocationBattery.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'OsmAndLocationLocationCoords':
          return OsmAndLocationLocationCoords.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'OsmAndQuery':
          return OsmAndQuery.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'OsmInfrastructurePayload':
          return OsmInfrastructurePayload.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'OutboundSettings':
          return OutboundSettings.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'OutletConsensus':
          return OutletConsensus.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'OutletConsensusProvidersInner':
          return OutletConsensusProvidersInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'OutlookPublishedEvent':
          return OutlookPublishedEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'OutlookWindow':
          return OutlookWindow.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'OverlandAck':
          return OverlandAck.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'OverlandBatch':
          return OverlandBatch.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'OverlandBatchLocationsInner':
          return OverlandBatchLocationsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'OverlandBatchLocationsInnerProperties':
          return OverlandBatchLocationsInnerProperties.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'OwnTracksMessage':
          return OwnTracksMessage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'OwnershipChain':
          return OwnershipChain.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'OwnershipChainNode':
          return OwnershipChainNode.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'OwnershipChainNodePathsInner':
          return OwnershipChainNodePathsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'OwnershipChainRequest':
          return OwnershipChainRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PackSpec':
          return PackSpec.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PackSpecRaster':
          return PackSpecRaster.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PackSpecVector':
          return PackSpecVector.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PackageManifest':
          return PackageManifest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PackageManifestAuthorsInner':
          return PackageManifestAuthorsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PackageManifestSender':
          return PackageManifestSender.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PackageReceivedEvent':
          return PackageReceivedEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PackageRecipient':
          return PackageRecipient.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PairingClaim':
          return PairingClaim.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PairingCode':
          return PairingCode.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PairingCodeCreated':
          return PairingCodeCreated.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PairingCodeInput':
          return PairingCodeInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ParamSpec':
          return ParamSpec.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Peer':
          return Peer.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PeerCreate':
          return PeerCreate.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PeerIngestKey':
          return PeerIngestKey.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PeerPage':
          return PeerPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PeerSelection':
          return PeerSelection.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PeerTestResult':
          return PeerTestResult.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PeerUpdate':
          return PeerUpdate.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PersistReason':
          
          
        case 'PersistReasonCounts':
          return PersistReasonCounts.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Place':
          return Place.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PlaceInput':
          return PlaceInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PlaceInputAttributes':
          return PlaceInputAttributes.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PlacePackRequest':
          return PlacePackRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PlacePage':
          return PlacePage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PlaceWeather':
          return PlaceWeather.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PlaceWeatherAurora':
          return PlaceWeatherAurora.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PlaceWeatherCell':
          return PlaceWeatherCell.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PlaceWeatherDigest':
          return PlaceWeatherDigest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PlaceWeatherDigestNext':
          return PlaceWeatherDigestNext.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PlaceWeatherDigestNextGfs':
          return PlaceWeatherDigestNextGfs.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PlaceWeatherDigestNextHrrr':
          return PlaceWeatherDigestNextHrrr.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PlaceWeatherDigestNextNws':
          return PlaceWeatherDigestNextNws.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PlaceWeatherDigestNow':
          return PlaceWeatherDigestNow.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PlaceWeatherDigestNowAlertsInner':
          return PlaceWeatherDigestNowAlertsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PlaceWeatherDigestNowAlertsStatus':
          return PlaceWeatherDigestNowAlertsStatus.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PlaceWeatherDigestNowLightning':
          return PlaceWeatherDigestNowLightning.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PlaceWeatherDigestNowRadar':
          return PlaceWeatherDigestNowRadar.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PlaceWeatherDigestPeriod':
          return PlaceWeatherDigestPeriod.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PlaceWeatherDigestPeriodHazardsEnteredInner':
          return PlaceWeatherDigestPeriodHazardsEnteredInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PlaceWeatherForecast':
          return PlaceWeatherForecast.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PlaceWeatherForecastGfs':
          return PlaceWeatherForecastGfs.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PlaceWeatherForecastNws':
          return PlaceWeatherForecastNws.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PlaceWeatherHail':
          return PlaceWeatherHail.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PlaceWeatherHeat':
          return PlaceWeatherHeat.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PlaceWeatherLightning':
          return PlaceWeatherLightning.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PlaceWeatherMarine':
          return PlaceWeatherMarine.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PlaceWeatherPrecip':
          return PlaceWeatherPrecip.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PlaceWeatherRadar':
          return PlaceWeatherRadar.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PlaceWeatherResponse':
          return PlaceWeatherResponse.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PlaceWeatherSmoke':
          return PlaceWeatherSmoke.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PlaceWeatherUv':
          return PlaceWeatherUv.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PlacesAoi':
          return PlacesAoi.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Plugin':
          return Plugin.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PluginList':
          return PluginList.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PluginSettings':
          return PluginSettings.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PluginUpdate':
          return PluginUpdate.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PolDeviation':
          return PolDeviation.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PolDeviationStay':
          return PolDeviationStay.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PolPlace':
          return PolPlace.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PolPlaceInput':
          return PolPlaceInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PolPlacePage':
          return PolPlacePage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PolStay':
          return PolStay.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PolStayPage':
          return PolStayPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PolSubject':
          return PolSubject.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PolSubjectInput':
          return PolSubjectInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PolSubjectInputParams':
          return PolSubjectInputParams.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PolSubjectPage':
          return PolSubjectPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PopulationPackImport':
          return PopulationPackImport.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PopulationPackImportCells':
          return PopulationPackImportCells.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PopulationPackImportRequest':
          return PopulationPackImportRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PortCallPayload':
          return PortCallPayload.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Precision':
          return Precision.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PrecisionComponentsInner':
          return PrecisionComponentsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Precursor':
          return Precursor.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PrecursorBatch':
          return PrecursorBatch.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PrecursorBatchPage':
          return PrecursorBatchPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PrecursorCandidate':
          return PrecursorCandidate.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PrecursorCandidateInput':
          return PrecursorCandidateInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PrecursorCandidatePage':
          return PrecursorCandidatePage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PrecursorEvent':
          return PrecursorEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PrecursorEventPrecursor':
          return PrecursorEventPrecursor.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PrecursorHistoryInner':
          return PrecursorHistoryInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PrecursorLagDistribution':
          return PrecursorLagDistribution.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PrecursorPage':
          return PrecursorPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PrecursorPatch':
          return PrecursorPatch.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PrecursorSide':
          return PrecursorSide.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PrecursorSpatial':
          return PrecursorSpatial.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PrecursorTest':
          return PrecursorTest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PrecursorTestAntecedent':
          return PrecursorTestAntecedent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PrecursorTestAntecedentOneOf':
          return PrecursorTestAntecedentOneOf.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PrecursorTestAntecedentOneOf1':
          return PrecursorTestAntecedentOneOf1.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PrecursorTestAntecedentOneOf2':
          return PrecursorTestAntecedentOneOf2.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PrecursorTestAntecedentOneOf3':
          return PrecursorTestAntecedentOneOf3.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PrecursorTestInput':
          return PrecursorTestInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PrecursorTestPage':
          return PrecursorTestPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PrecursorTestResult':
          return PrecursorTestResult.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PrecursorTestResultHitRateCi':
          return PrecursorTestResultHitRateCi.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PrecursorTestResultLag':
          return PrecursorTestResultLag.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PrecursorTestResultMultipleTesting':
          return PrecursorTestResultMultipleTesting.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PrecursorTestResultReverse':
          return PrecursorTestResultReverse.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PrecursorTestRunRequest':
          return PrecursorTestRunRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PrecursorTestSpatial':
          return PrecursorTestSpatial.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PrecursorTestSpatialOneOf':
          return PrecursorTestSpatialOneOf.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PrecursorTestSpatialOneOf1':
          return PrecursorTestSpatialOneOf1.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PrecursorWindowStats':
          return PrecursorWindowStats.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PrecursorWindowStatsReverse':
          return PrecursorWindowStatsReverse.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PresenceEvent':
          return PresenceEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PresenceEventChange':
          return PresenceEventChange.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PresenceEventClientsInner':
          return PresenceEventClientsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Principal':
          return Principal.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PrincipalInput':
          return PrincipalInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PrincipalPatch':
          return PrincipalPatch.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PrincipalSummary':
          return PrincipalSummary.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PrincipalSummaryPage':
          return PrincipalSummaryPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ProbSeverePayload':
          return ProbSeverePayload.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ProbSeverePayloadMotion':
          return ProbSeverePayloadMotion.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ProbeResult':
          return ProbeResult.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ProbeResultRequestsInner':
          return ProbeResultRequestsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Problem':
          return Problem.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ProblemErrorsInner':
          return ProblemErrorsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ProblemReferences':
          return ProblemReferences.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Product':
          return Product.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ProductExportRequest':
          return ProductExportRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ProductLegend':
          return ProductLegend.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ProductLinks':
          return ProductLinks.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ProductLoop':
          return ProductLoop.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ProductLoopFramesInner':
          return ProductLoopFramesInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ProductPage':
          return ProductPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ProductRender':
          return ProductRender.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ProductSpatial':
          return ProductSpatial.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ProductStatus':
          return ProductStatus.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ProductTemporal':
          return ProductTemporal.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ProductTemporalRetention':
          return ProductTemporalRetention.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ProductTemporalRetentionLadderInner':
          return ProductTemporalRetentionLadderInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ProductTemporalRunsInner':
          return ProductTemporalRunsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ProductTimeline':
          return ProductTimeline.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ProjectedImpact':
          return ProjectedImpact.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ProjectedImpactDependencyPathsInner':
          return ProjectedImpactDependencyPathsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ProjectedImpactExposedAssetsInner':
          return ProjectedImpactExposedAssetsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ProjectedImpactInputsInner':
          return ProjectedImpactInputsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ProjectedImpactInterval':
          return ProjectedImpactInterval.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ProjectedImpactPage':
          return ProjectedImpactPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ProjectedImpactQuestionWindow':
          return ProjectedImpactQuestionWindow.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ProjectedImpactSkippedInner':
          return ProjectedImpactSkippedInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PromptTemplate':
          return PromptTemplate.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PromptTemplateList':
          return PromptTemplateList.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PromptTemplateSummary':
          return PromptTemplateSummary.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PropagationEdge':
          return PropagationEdge.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PropagationGraph':
          return PropagationGraph.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PropagationGraphCrossPlatformOrderInner':
          return PropagationGraphCrossPlatformOrderInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PropagationGraphEarliestByPlatformInner':
          return PropagationGraphEarliestByPlatformInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PropagationGraphFirstNewsPickup':
          return PropagationGraphFirstNewsPickup.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PropagationNode':
          return PropagationNode.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ProposedPatch':
          return ProposedPatch.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ProposedPatchSanityChecks':
          return ProposedPatchSanityChecks.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ProtectedResourceMetadata':
          return ProtectedResourceMetadata.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Provenance':
          return Provenance.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ProvenanceChainEntry':
          return ProvenanceChainEntry.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ProvenanceHop':
          return ProvenanceHop.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PurgeReport':
          return PurgeReport.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PurgeReportCitingDocuments':
          return PurgeReportCitingDocuments.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PurgeRequest':
          return PurgeRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PushDevice':
          return PushDevice.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PushDeviceInput':
          return PushDeviceInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PushDeviceUpdate':
          return PushDeviceUpdate.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PushFilters':
          return PushFilters.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PushPayload':
          return PushPayload.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PushPayloadActionsInner':
          return PushPayloadActionsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PushPayloadLinks':
          return PushPayloadLinks.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PushProvider':
          return PushProvider.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PushProviderInput':
          return PushProviderInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PushProviderInputConfig':
          return PushProviderInputConfig.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PushProviderInputSecrets':
          return PushProviderInputSecrets.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PushProviderTestRequest':
          return PushProviderTestRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PushSettings':
          return PushSettings.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PushSubscription':
          return PushSubscription.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PushSubscriptionInput':
          return PushSubscriptionInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PushSubscriptionInputSubscription':
          return PushSubscriptionInputSubscription.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PushSubscriptionInputSubscriptionKeys':
          return PushSubscriptionInputSubscriptionKeys.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PushSubscriptionVerify':
          return PushSubscriptionVerify.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'PushTestResult':
          return PushTestResult.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Quality':
          return Quality.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Quantiles':
          return Quantiles.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'QueryHistory200Response':
          return QueryHistory200Response.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'QuestionAnnulment':
          return QuestionAnnulment.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'QuestionLinks':
          return QuestionLinks.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'QuestionOutcomes':
          return QuestionOutcomes.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'QuietHours':
          return QuietHours.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RadarCellPayload':
          return RadarCellPayload.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RadarCellPayloadMotion':
          return RadarCellPayloadMotion.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RasterFrame':
          return RasterFrame.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RasterFrameEvent':
          return RasterFrameEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RasterFrameForecastRun':
          return RasterFrameForecastRun.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RasterFrameObservation':
          return RasterFrameObservation.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RasterFramePage':
          return RasterFramePage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RasterFramePagePrefetch':
          return RasterFramePagePrefetch.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RateLimitSettings':
          return RateLimitSettings.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RateLimitSettingsStream':
          return RateLimitSettingsStream.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RatingsImportResult':
          return RatingsImportResult.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RatingsImportResultErrorsInner':
          return RatingsImportResultErrorsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ReadRestriction':
          return ReadRestriction.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ReceivedPackage':
          return ReceivedPackage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ReceivedPackageContentsInner':
          return ReceivedPackageContentsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ReceivedPackagePage':
          return ReceivedPackagePage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ReceivedPackageSender':
          return ReceivedPackageSender.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RecipeBacktestResult':
          return RecipeBacktestResult.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RecipeBacktestResultEventsInner':
          return RecipeBacktestResultEventsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RecipeBacktestResultOverlap':
          return RecipeBacktestResultOverlap.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RecipeTestRequest':
          return RecipeTestRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RecipeTestResult':
          return RecipeTestResult.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Record':
          return Record.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RecordEvidence':
          return RecordEvidence.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RecordMapping':
          return RecordMapping.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RecordMappingFilterInner':
          return RecordMappingFilterInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RecordMappingGeometry':
          return RecordMappingGeometry.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RecordMappingGeometryOneOf':
          return RecordMappingGeometryOneOf.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RecordMappingGeometryOneOf1':
          return RecordMappingGeometryOneOf1.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RecordMappingGeometryOneOf2':
          return RecordMappingGeometryOneOf2.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RecordMappingPairs':
          return RecordMappingPairs.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RecordMappingQuality':
          return RecordMappingQuality.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RecordMappingSpread':
          return RecordMappingSpread.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RecordMappingTombstone':
          return RecordMappingTombstone.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RecordPage':
          return RecordPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RecordVersion':
          return RecordVersion.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RecordVersionPage':
          return RecordVersionPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ReferenceArea':
          return ReferenceArea.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ReferenceAreaOutline':
          return ReferenceAreaOutline.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ReferenceDataset':
          return ReferenceDataset.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ReferenceImport':
          return ReferenceImport.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ReferenceImportPage':
          return ReferenceImportPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ReferenceImportRequest':
          return ReferenceImportRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ReferenceImportRequestParams':
          return ReferenceImportRequestParams.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ReferenceLoadRequest':
          return ReferenceLoadRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ReferenceStatus':
          return ReferenceStatus.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RegionNear':
          return RegionNear.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RegionProfile':
          return RegionProfile.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RegionTags':
          return RegionTags.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RegionalIndexDef':
          return RegionalIndexDef.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RegionalIndexDefComponentsInner':
          return RegionalIndexDefComponentsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RegionalIndexDefFloorsInner':
          return RegionalIndexDefFloorsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RegionalIndexDefInput':
          return RegionalIndexDefInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RegionalIndexDefPage':
          return RegionalIndexDefPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RegionalIndexDefSkill':
          return RegionalIndexDefSkill.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RegionalIndexDetail':
          return RegionalIndexDetail.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RegionalIndexValue':
          return RegionalIndexValue.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RegionalIndexValueComponentsInner':
          return RegionalIndexValueComponentsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RegionalOutlook':
          return RegionalOutlook.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RegionalOutlookPage':
          return RegionalOutlookPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RegionalOutlookSkill':
          return RegionalOutlookSkill.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RegistrationToken':
          return RegistrationToken.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RegistrationTokenCreated':
          return RegistrationTokenCreated.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RegistrationTokenInput':
          return RegistrationTokenInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RelatedObject':
          return RelatedObject.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RelatedObjects':
          return RelatedObjects.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Relation':
          return Relation.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RelationInput':
          return RelationInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RelationProvenance':
          return RelationProvenance.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RemoteMediaAccepted':
          return RemoteMediaAccepted.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RemoteMediaMeta':
          return RemoteMediaMeta.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RemoteRasterBundleAccepted':
          return RemoteRasterBundleAccepted.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RemoteRasterBundleMeta':
          return RemoteRasterBundleMeta.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RendezvousPayload':
          return RendezvousPayload.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ReplayResult':
          return ReplayResult.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Report':
          return Report.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ReportBlock':
          return ReportBlock.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ReportBlockBase':
          return ReportBlockBase.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ReportBlockOneOf':
          return ReportBlockOneOf.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ReportBlockOneOf1':
          return ReportBlockOneOf1.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ReportBlockOneOf10':
          return ReportBlockOneOf10.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ReportBlockOneOf10AllOfAttributionsInner':
          return ReportBlockOneOf10AllOfAttributionsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ReportBlockOneOf1AllOfJudgmentsInner':
          return ReportBlockOneOf1AllOfJudgmentsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ReportBlockOneOf2':
          return ReportBlockOneOf2.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ReportBlockOneOf2AllOfItemsInner':
          return ReportBlockOneOf2AllOfItemsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ReportBlockOneOf3':
          return ReportBlockOneOf3.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ReportBlockOneOf4':
          return ReportBlockOneOf4.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ReportBlockOneOf4AllOfItemsInner':
          return ReportBlockOneOf4AllOfItemsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ReportBlockOneOf5':
          return ReportBlockOneOf5.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ReportBlockOneOf6':
          return ReportBlockOneOf6.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ReportBlockOneOf6AllOfQuestionsInner':
          return ReportBlockOneOf6AllOfQuestionsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ReportBlockOneOf7':
          return ReportBlockOneOf7.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ReportBlockOneOf7AllOfItemsInner':
          return ReportBlockOneOf7AllOfItemsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ReportBlockOneOf8':
          return ReportBlockOneOf8.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ReportBlockOneOf9':
          return ReportBlockOneOf9.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ReportBundle':
          return ReportBundle.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ReportBundleAttachmentsInner':
          return ReportBundleAttachmentsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ReportDraftRequest':
          return ReportDraftRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ReportEvent':
          return ReportEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ReportInput':
          return ReportInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ReportPage':
          return ReportPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ReportPatch':
          return ReportPatch.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ReportRetractRequest':
          return ReportRetractRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ReportRevision':
          return ReportRevision.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ReportRevisionPage':
          return ReportRevisionPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ReportRevisionRenditions':
          return ReportRevisionRenditions.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ReportSend':
          return ReportSend.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ReportSendInput':
          return ReportSendInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ReportSendRecipientsInner':
          return ReportSendRecipientsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ReportShareLinkInput':
          return ReportShareLinkInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ReportSource':
          return ReportSource.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ResetEvent':
          return ResetEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ResolverArea':
          return ResolverArea.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ResyncEvent':
          return ResyncEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RetentionRunResult':
          return RetentionRunResult.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RetentionRunResultConsumerClamp':
          return RetentionRunResultConsumerClamp.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RetentionRunResultPerSourceValue':
          return RetentionRunResultPerSourceValue.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RetentionSettings':
          return RetentionSettings.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RetentionSettingsSocial':
          return RetentionSettingsSocial.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RetentionSettingsStats':
          return RetentionSettingsStats.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RevisionDriver':
          return RevisionDriver.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RuleBacktestResult':
          return RuleBacktestResult.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RuleBacktestResultActual':
          return RuleBacktestResultActual.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RuleBacktestResultAnomaly':
          return RuleBacktestResultAnomaly.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RuleBacktestResultOverlap':
          return RuleBacktestResultOverlap.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RuleBacktestResultPerDayInner':
          return RuleBacktestResultPerDayInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RuleBacktestResultSamplesInner':
          return RuleBacktestResultSamplesInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RuleBacktestResultTopEntitiesInner':
          return RuleBacktestResultTopEntitiesInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RuleBacktestResultVariantsInner':
          return RuleBacktestResultVariantsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RuleBacktestResultWindow':
          return RuleBacktestResultWindow.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RuleQuality':
          return RuleQuality.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RuleQualityDaily':
          return RuleQualityDaily.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RuleQualityDailyFeedback':
          return RuleQualityDailyFeedback.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RuleQualityDailySignals':
          return RuleQualityDailySignals.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RuleQualityPrecisionCi':
          return RuleQualityPrecisionCi.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'RuleQualitySignals':
          return RuleQualitySignals.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SampleBatchResponse':
          return SampleBatchResponse.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SamplePosition':
          return SamplePosition.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SampleRequest':
          return SampleRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SampleRequestPointsInner':
          return SampleRequestPointsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SampleRequestPointsInnerOneOf':
          return SampleRequestPointsInnerOneOf.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SampleRequestPointsInnerOneOf1':
          return SampleRequestPointsInnerOneOf1.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SampleResponse':
          return SampleResponse.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SampleStatus':
          
          
        case 'SampleTrackRequest':
          return SampleTrackRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SampleTrackRequestPointsInner':
          return SampleTrackRequestPointsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SampleTrackResponse':
          return SampleTrackResponse.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SampleTrackResponsePointsInner':
          return SampleTrackResponsePointsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SampleValue':
          return SampleValue.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SampleValueDerived':
          return SampleValueDerived.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SatellitePassPayload':
          return SatellitePassPayload.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SatellitePassPayloadProductsInner':
          return SatellitePassPayloadProductsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SatellitePassPayloadRf':
          return SatellitePassPayloadRf.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SatellitePassPayloadRfSnrByElevationInner':
          return SatellitePassPayloadRfSnrByElevationInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SavedSearch':
          return SavedSearch.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SavedSearchHit':
          return SavedSearchHit.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SavedSearchInput':
          return SavedSearchInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SavedSearchMatchesEvent':
          return SavedSearchMatchesEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SavedSearchPage':
          return SavedSearchPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SavedSearchPatch':
          return SavedSearchPatch.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SavedSearchQuery':
          return SavedSearchQuery.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SavedSearchQueryArea':
          return SavedSearchQueryArea.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SavedSearchQueryTimeWindow':
          return SavedSearchQueryTimeWindow.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SavedSearchResultPage':
          return SavedSearchResultPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SavedView':
          return SavedView.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SavedViewInput':
          return SavedViewInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SavedViewPage':
          return SavedViewPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ScorecardEntry':
          return ScorecardEntry.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ScorecardEntryBias':
          return ScorecardEntryBias.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ScorecardEntryIntervalCoverage':
          return ScorecardEntryIntervalCoverage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ScorecardEntryMurphy':
          return ScorecardEntryMurphy.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ScorecardEntryRecalibration':
          return ScorecardEntryRecalibration.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ScorecardEntrySkill':
          return ScorecardEntrySkill.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Search200Response':
          return Search200Response.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SearchReindexState':
          return SearchReindexState.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SearchResult':
          return SearchResult.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SearchResults':
          return SearchResults.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SearchStatus':
          return SearchStatus.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SecuritySettings':
          return SecuritySettings.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Sensor':
          return Sensor.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SensorAllOfCapabilities':
          return SensorAllOfCapabilities.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SensorAllOfPasses':
          return SensorAllOfPasses.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SensorDiscoveryResult':
          return SensorDiscoveryResult.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Series':
          return Series.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SeriesCatalogEntry':
          return SeriesCatalogEntry.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SeriesData':
          return SeriesData.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SeriesForecast':
          return SeriesForecast.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SeriesForecastForecastsInner':
          return SeriesForecastForecastsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SeriesForecastModel':
          return SeriesForecastModel.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SeriesForecastModelBacktest':
          return SeriesForecastModelBacktest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SeriesForecastModelBacktestResultsInner':
          return SeriesForecastModelBacktestResultsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SeriesInfo':
          return SeriesInfo.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SeriesPage':
          return SeriesPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SeriesPoint':
          return SeriesPoint.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SeriesPointPage':
          return SeriesPointPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SeriesSample':
          return SeriesSample.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ServerSettings':
          return ServerSettings.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SettingsBundle':
          return SettingsBundle.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SettingsBundleChannelsInner':
          return SettingsBundleChannelsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SettingsBundleWatchlistsInner':
          return SettingsBundleWatchlistsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SettingsWarning':
          return SettingsWarning.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Severity':
          
          
        case 'ShareLink':
          return ShareLink.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ShareLinkCreate':
          return ShareLinkCreate.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ShareLinkCreated':
          return ShareLinkCreated.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ShareLinkPage':
          return ShareLinkPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ShareReceivedEvent':
          return ShareReceivedEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ShareRequest':
          return ShareRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'ShareableType':
          
          
        case 'SharedContent':
          return SharedContent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SharedContentLinks':
          return SharedContentLinks.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SharedItem':
          return SharedItem.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SharedItemPage':
          return SharedItemPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SignatureVerifierConfig':
          return SignatureVerifierConfig.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SigningKeyInfo':
          return SigningKeyInfo.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Sink':
          return Sink.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SinkCloudEvent':
          return SinkCloudEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SinkCloudEventData':
          return SinkCloudEventData.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SinkCreate':
          return SinkCreate.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SinkFilter':
          return SinkFilter.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SinkPage':
          return SinkPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SinkStatus':
          return SinkStatus.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SinkStatusClients':
          return SinkStatusClients.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SinkStatusClientsPeersInner':
          return SinkStatusClientsPeersInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SinkStatusDenied':
          return SinkStatusDenied.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SinkTestRequest':
          return SinkTestRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SinkTestResult':
          return SinkTestResult.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SinkType':
          return SinkType.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SinkUpdate':
          return SinkUpdate.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Situation':
          return Situation.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SituationAssessmentRequest':
          return SituationAssessmentRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SituationBriefStatus':
          return SituationBriefStatus.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SituationChanges':
          return SituationChanges.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SituationChangesArea':
          return SituationChangesArea.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SituationChangesKeyValuesInner':
          return SituationChangesKeyValuesInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SituationChangesNewMembers':
          return SituationChangesNewMembers.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SituationChangesSeverity':
          return SituationChangesSeverity.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SituationChangesStatus':
          return SituationChangesStatus.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SituationEvent':
          return SituationEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SituationEvidence':
          return SituationEvidence.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SituationEvidenceCorroboration':
          return SituationEvidenceCorroboration.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SituationEvidenceFamiliesInner':
          return SituationEvidenceFamiliesInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SituationEvidenceMemberCounts':
          return SituationEvidenceMemberCounts.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SituationInference':
          return SituationInference.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SituationInput':
          return SituationInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SituationJoinReason':
          return SituationJoinReason.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SituationJoinReasonComponents':
          return SituationJoinReasonComponents.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SituationLinkDecision':
          return SituationLinkDecision.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SituationLinkDecisionCandidatesInner':
          return SituationLinkDecisionCandidatesInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SituationLinkerTestRequest':
          return SituationLinkerTestRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SituationLinkerTestResult':
          return SituationLinkerTestResult.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SituationMember':
          return SituationMember.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SituationMemberInput':
          return SituationMemberInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SituationMemberPage':
          return SituationMemberPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SituationMergeInput':
          return SituationMergeInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SituationPage':
          return SituationPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SituationPatch':
          return SituationPatch.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SituationSocialSection':
          return SituationSocialSection.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SituationSocialSectionAttentionGap':
          return SituationSocialSectionAttentionGap.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SituationSocialSectionNarrativesInner':
          return SituationSocialSectionNarrativesInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SituationSpeculation':
          return SituationSpeculation.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SituationSplitInput':
          return SituationSplitInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SituationStatus':
          
          
        case 'SituationSummary':
          return SituationSummary.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SituationTheme':
          
          
        case 'SituationTimelineEntry':
          return SituationTimelineEntry.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SituationTimelinePage':
          return SituationTimelinePage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SmokePolygonPayload':
          return SmokePolygonPayload.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Snapshot':
          return Snapshot.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SnapshotInput':
          return SnapshotInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SnoozeInput':
          return SnoozeInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialAuthor':
          return SocialAuthor.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialCollectionPath':
          return SocialCollectionPath.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialCollectionType':
          
          
        case 'SocialComparison':
          return SocialComparison.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialComparisonContradiction':
          return SocialComparisonContradiction.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialComparisonDetectability':
          return SocialComparisonDetectability.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialComparisonEvent':
          return SocialComparisonEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialComparisonPage':
          return SocialComparisonPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialComparisonRevision':
          return SocialComparisonRevision.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialCoverage':
          return SocialCoverage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialCoverageBucket':
          return SocialCoverageBucket.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialCoveragePage':
          return SocialCoveragePage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialCue':
          return SocialCue.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialCueEvent':
          return SocialCueEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialCueExternalResultsInner':
          return SocialCueExternalResultsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialCueInput':
          return SocialCueInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialCuePage':
          return SocialCuePage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialCueQuery':
          return SocialCueQuery.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialCueResultsInner':
          return SocialCueResultsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialCueTrigger':
          return SocialCueTrigger.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialEngagement':
          return SocialEngagement.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialLeadTimes':
          return SocialLeadTimes.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialPhysicalEvidence':
          return SocialPhysicalEvidence.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialPlatform':
          return SocialPlatform.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialPlatformCredentials':
          return SocialPlatformCredentials.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialPlatformDeletionSync':
          return SocialPlatformDeletionSync.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialPlatformId':
          
          
        case 'SocialPlatformPrice':
          return SocialPlatformPrice.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialPlatformSettings':
          return SocialPlatformSettings.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialPlatformSettingsOptions':
          return SocialPlatformSettingsOptions.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialPlatformStateEvent':
          return SocialPlatformStateEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialPlatformStream':
          return SocialPlatformStream.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialPlatformStreamLastGap':
          return SocialPlatformStreamLastGap.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialPlatformTerms':
          return SocialPlatformTerms.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialPost':
          return SocialPost.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialPostGeo':
          return SocialPostGeo.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialPostLink':
          return SocialPostLink.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialPostMedia':
          return SocialPostMedia.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialPostPage':
          return SocialPostPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialPostPayload':
          return SocialPostPayload.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialQuota':
          return SocialQuota.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialSettings':
          return SocialSettings.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialSettingsAttentionGap':
          return SocialSettingsAttentionGap.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialSettingsCompare':
          return SocialSettingsCompare.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialSettingsCompareDefaultWindow':
          return SocialSettingsCompareDefaultWindow.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialSettingsCompareFamilyWindowsValue':
          return SocialSettingsCompareFamilyWindowsValue.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialSettingsCues':
          return SocialSettingsCues.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialSettingsCuesTriggers':
          return SocialSettingsCuesTriggers.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialSettingsCuesXaiXSearch':
          return SocialSettingsCuesXaiXSearch.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialSettingsTrackRecord':
          return SocialSettingsTrackRecord.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialSettingsTrackRecordOverridesInner':
          return SocialSettingsTrackRecordOverridesInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialSettingsVelocity':
          return SocialSettingsVelocity.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialSpend':
          return SocialSpend.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialVelocityEvent':
          return SocialVelocityEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialVelocityEventScope':
          return SocialVelocityEventScope.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialVersusObserved':
          return SocialVersusObserved.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialVersusObservedLeadTimesInner':
          return SocialVersusObservedLeadTimesInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialVersusObservedNarrativesInner':
          return SocialVersusObservedNarrativesInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialVolume':
          return SocialVolume.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialVolumePoint':
          return SocialVolumePoint.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialVolumeScope':
          return SocialVolumeScope.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SocialVolumeSeries':
          return SocialVolumeSeries.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SourceAccess':
          return SourceAccess.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SourceDescriptor':
          return SourceDescriptor.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SourceDetail':
          return SourceDetail.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SourceHealth':
          return SourceHealth.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SourceRelevance':
          return SourceRelevance.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SourceRelevanceAnalysis':
          return SourceRelevanceAnalysis.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SourceRelevanceAnalysisShared':
          return SourceRelevanceAnalysisShared.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SourceRelevanceAnalysisTemplate':
          return SourceRelevanceAnalysisTemplate.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SourceRelevanceRequest':
          return SourceRelevanceRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SourceSettings':
          return SourceSettings.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SourceSettingsUpdate':
          return SourceSettingsUpdate.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SourceStorage':
          return SourceStorage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SourceSummary':
          return SourceSummary.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SpcOutlookPayload':
          return SpcOutlookPayload.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'StatInterval':
          return StatInterval.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'StatsGrid':
          return StatsGrid.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'StatsGridFeaturesInner':
          return StatsGridFeaturesInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'StatsGridFeaturesInnerProperties':
          return StatsGridFeaturesInnerProperties.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'StatsRequest':
          return StatsRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'StatsResponse':
          return StatsResponse.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'StatsResponseThresholdsInner':
          return StatsResponseThresholdsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'StatsRollupRebuild':
          return StatsRollupRebuild.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'StatsRollupRebuildRequest':
          return StatsRollupRebuildRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'StatsSeries':
          return StatsSeries.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'StatsSeriesSeriesInner':
          return StatsSeriesSeriesInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'StatsSeriesSeriesInnerPointsInner':
          return StatsSeriesSeriesInnerPointsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'StatsTop':
          return StatsTop.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'StatsTopItemsInner':
          return StatsTopItemsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'StixBundle':
          return StixBundle.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'StormReportPayload':
          return StormReportPayload.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'StormSwathPayload':
          return StormSwathPayload.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'StormTrackPayload':
          return StormTrackPayload.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'StormTrackPayloadCell':
          return StormTrackPayloadCell.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'StormTrackPayloadHistoryInner':
          return StormTrackPayloadHistoryInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'StormTrackPayloadLightningJump':
          return StormTrackPayloadLightningJump.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'StormTrackPayloadMotion':
          return StormTrackPayloadMotion.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'StormTrackPayloadNowcastInner':
          return StormTrackPayloadNowcastInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'StormTrackPayloadTrend':
          return StormTrackPayloadTrend.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'StreamAlerts200Response':
          return StreamAlerts200Response.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'StreamChanges200Response':
          return StreamChanges200Response.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'StreamEvents200Response':
          return StreamEvents200Response.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'StreamFederation200Response':
          return StreamFederation200Response.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'StreamNotifications200Response':
          return StreamNotifications200Response.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'StreamTicket':
          return StreamTicket.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'StreamTicketRequest':
          return StreamTicketRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'StreamView200Response':
          return StreamView200Response.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'StructureDiff':
          return StructureDiff.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'StructureDiffContentTypeChanged':
          return StructureDiffContentTypeChanged.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'StructureDiffDelimiterChanged':
          return StructureDiffDelimiterChanged.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'StructureDiffPresenceDroppedInner':
          return StructureDiffPresenceDroppedInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'StructureDiffRenameCandidatesInner':
          return StructureDiffRenameCandidatesInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'StructureDiffTypeChangedInner':
          return StructureDiffTypeChangedInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Style':
          return Style.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'StyleCategory':
          return StyleCategory.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'StyleFragment':
          return StyleFragment.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'StyleFragmentMetadata':
          return StyleFragmentMetadata.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'StyleMeaning':
          return StyleMeaning.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'StylePage':
          return StylePage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'StyleSpecial':
          return StyleSpecial.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'StyleStop':
          return StyleStop.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SystemInfo':
          return SystemInfo.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SystemInfoArchive':
          return SystemInfoArchive.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SystemInfoCatchUp':
          return SystemInfoCatchUp.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SystemInfoDatabase':
          return SystemInfoDatabase.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SystemInfoExports':
          return SystemInfoExports.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SystemInfoHistory':
          return SystemInfoHistory.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SystemInfoHistoryTiersInner':
          return SystemInfoHistoryTiersInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SystemInfoHolds':
          return SystemInfoHolds.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SystemInfoIngestWriter':
          return SystemInfoIngestWriter.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SystemInfoLimits':
          return SystemInfoLimits.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SystemInfoLlm':
          return SystemInfoLlm.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SystemInfoMcp':
          return SystemInfoMcp.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SystemInfoMcpTransports':
          return SystemInfoMcpTransports.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SystemInfoNotifications':
          return SystemInfoNotifications.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SystemInfoRasterFormats':
          return SystemInfoRasterFormats.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SystemInfoRasterFormatsWebp':
          return SystemInfoRasterFormatsWebp.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SystemInfoRecords':
          return SystemInfoRecords.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SystemInfoScheduler':
          return SystemInfoScheduler.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SystemInfoSearch':
          return SystemInfoSearch.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SystemInfoSecurity':
          return SystemInfoSecurity.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SystemInfoSinks':
          return SystemInfoSinks.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SystemInfoSources':
          return SystemInfoSources.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SystemInfoStreams':
          return SystemInfoStreams.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SystemInfoWarningsInner':
          return SystemInfoWarningsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SystemRasters':
          return SystemRasters.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SystemRastersCachesValue':
          return SystemRastersCachesValue.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SystemRastersGlm':
          return SystemRastersGlm.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'SystemRastersRenderQueue':
          return SystemRastersRenderQueue.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TafPayload':
          return TafPayload.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TafPayloadForecastInner':
          return TafPayloadForecastInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Tag':
          return Tag.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TagAssignResult':
          return TagAssignResult.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TagAssignResultSkippedInner':
          return TagAssignResultSkippedInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TagAssignment':
          return TagAssignment.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TagInput':
          return TagInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TagPage':
          return TagPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TagPatch':
          return TagPatch.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TagRefsRequest':
          return TagRefsRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TaxiiApiRoot':
          return TaxiiApiRoot.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TaxiiCollection':
          return TaxiiCollection.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TaxiiCollections':
          return TaxiiCollections.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TaxiiDiscovery':
          return TaxiiDiscovery.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TaxiiEnvelope':
          return TaxiiEnvelope.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TileJson':
          return TileJson.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TileJsonVectorLayersInner':
          return TileJsonVectorLayersInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TileKey':
          return TileKey.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TileKeyCreate':
          return TileKeyCreate.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TileKeyCreated':
          return TileKeyCreated.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TileKeyPage':
          return TileKeyPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TileLayerIndex':
          return TileLayerIndex.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TileLayerIndexLayersInner':
          return TileLayerIndexLayersInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TimeRange':
          return TimeRange.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Timeline':
          return Timeline.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TimelineAlertsInner':
          return TimelineAlertsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TimelineBucket':
          return TimelineBucket.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TimelineTick':
          return TimelineTick.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TimelineTickFramesInner':
          return TimelineTickFramesInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Tlp':
          
          
        case 'TokenRestrictions':
          return TokenRestrictions.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TouchEvent':
          return TouchEvent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Track':
          return Track.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TrackProperties':
          return TrackProperties.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TransitCatalogEntry':
          return TransitCatalogEntry.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TransitCatalogPage':
          return TransitCatalogPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TriggerMuteInput':
          return TriggerMuteInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TriggerMuteInputOneOf':
          return TriggerMuteInputOneOf.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TriggerMuteInputOneOf1':
          return TriggerMuteInputOneOf1.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TriggerRule':
          return TriggerRule.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TriggerRuleAllOfFeedbackCounts':
          return TriggerRuleAllOfFeedbackCounts.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TriggerRuleInput':
          return TriggerRuleInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TriggerRuleInputAutoResolve':
          return TriggerRuleInputAutoResolve.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TriggerRuleInputConditionsInner':
          return TriggerRuleInputConditionsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TriggerRuleInputWindow':
          return TriggerRuleInputWindow.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TriggerTestResult':
          return TriggerTestResult.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TriggerTestResultWarningsInner':
          return TriggerTestResultWarningsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TriggerTestResultWindowEvaluationsInner':
          return TriggerTestResultWindowEvaluationsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Trip':
          return Trip.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TripCheckRequest':
          return TripCheckRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TripHazard':
          return TripHazard.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TripInput':
          return TripInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TripPage':
          return TripPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TripRun':
          return TripRun.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TripRunPage':
          return TripRunPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TripSchedule':
          return TripSchedule.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TuningLogEntry':
          return TuningLogEntry.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TuningLogPage':
          return TuningLogPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TuningSuggestion':
          return TuningSuggestion.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TuningSuggestionApply':
          return TuningSuggestionApply.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TuningSuggestionDismiss':
          return TuningSuggestionDismiss.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TuningSuggestionExpectedEffect':
          return TuningSuggestionExpectedEffect.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TuningSuggestionList':
          return TuningSuggestionList.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'TuningSuggestionRecord':
          return TuningSuggestionRecord.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'UpdateTokenRequest':
          return UpdateTokenRequest.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'UsagePoint':
          return UsagePoint.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'UsageSeries':
          return UsageSeries.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'UvForecastPayload':
          return UvForecastPayload.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'UvForecastPayloadHoursInner':
          return UvForecastPayloadHoursInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'VelocityJsonComponent':
          return VelocityJsonComponent.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'VelocityJsonComponentHeader':
          return VelocityJsonComponentHeader.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Visibility':
          
          
        case 'WatchPair':
          return WatchPair.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'WatchdogSettings':
          return WatchdogSettings.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'WatchdogSettingsCanary':
          return WatchdogSettingsCanary.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Watchlist':
          return Watchlist.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'WatchlistImportResult':
          return WatchlistImportResult.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'WatchlistImportResultInvalidInner':
          return WatchlistImportResultInvalidInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'WatchlistInput':
          return WatchlistInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'WatchlistItem':
          return WatchlistItem.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'WatchlistItemInput':
          return WatchlistItemInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'WatchlistItemPage':
          return WatchlistItemPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'WatchlistItemsInput':
          return WatchlistItemsInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'WatchlistPage':
          return WatchlistPage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'WeatherContext':
          return WeatherContext.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'WeatherContextLightning':
          return WeatherContextLightning.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'WeatherContextPrecip':
          return WeatherContextPrecip.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'WeatherContextRadar':
          return WeatherContextRadar.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'WeatherContextRadarNearestCell':
          return WeatherContextRadarNearestCell.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'WeatherContextRadarNearestCellMotion':
          return WeatherContextRadarNearestCellMotion.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'WeatherContextSmoke':
          return WeatherContextSmoke.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'WeatherContextWarnings':
          return WeatherContextWarnings.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'WeatherContextWarningsItemsInner':
          return WeatherContextWarningsItemsInner.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'WeatherContextWind':
          return WeatherContextWind.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'WeatherSectionStatus':
          
          
        case 'WeatherSettings':
          return WeatherSettings.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'WebhookEnvelope':
          return WebhookEnvelope.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'WindowSpecCommon':
          return WindowSpecCommon.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'WindowState':
          return WindowState.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'WindowStateList':
          return WindowStateList.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'WindowStatePayload':
          return WindowStatePayload.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'WindowStatus':
          return WindowStatus.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'WindowStatusStates':
          return WindowStatusStates.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'Workspace':
          return Workspace.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'WorkspaceContext':
          return WorkspaceContext.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'WorkspaceContextPlayback':
          return WorkspaceContextPlayback.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'WorkspaceContextState':
          return WorkspaceContextState.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'WorkspaceInput':
          return WorkspaceInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'WorkspacePage':
          return WorkspacePage.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'WorkspacePatch':
          return WorkspacePatch.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'WorkspaceShareInput':
          return WorkspaceShareInput.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'WorkspaceViewport':
          return WorkspaceViewport.fromJson(value as Map<String, dynamic>) as ReturnType;
        case 'WxAreaPayload':
          return WxAreaPayload.fromJson(value as Map<String, dynamic>) as ReturnType;
        default:
          RegExpMatch? match;

          if (value is List && (match = _regList.firstMatch(targetType)) != null) {
            targetType = match![1]!; // ignore: parameter_assignments
            return value
              .map<BaseType>((dynamic v) => deserialize<BaseType, BaseType>(v, targetType, growable: growable))
              .toList(growable: growable) as ReturnType;
          }
          if (value is Set && (match = _regSet.firstMatch(targetType)) != null) {
            targetType = match![1]!; // ignore: parameter_assignments
            return value
              .map<BaseType>((dynamic v) => deserialize<BaseType, BaseType>(v, targetType, growable: growable))
              .toSet() as ReturnType;
          }
          if (value is Map && (match = _regMap.firstMatch(targetType)) != null) {
            targetType = match![1]!.trim(); // ignore: parameter_assignments
            return Map<String, BaseType>.fromIterables(
              value.keys as Iterable<String>,
              value.values.map((dynamic v) => deserialize<BaseType, BaseType>(v, targetType, growable: growable)),
            ) as ReturnType;
          }
          break;
    }
    throw Exception('Cannot deserialize');
  }