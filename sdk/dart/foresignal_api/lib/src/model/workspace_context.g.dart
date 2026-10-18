// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'workspace_context.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$WorkspaceContextCWProxy {
  WorkspaceContext selectedRefs(List<String>? selectedRefs);

  WorkspaceContext focusRef(String? focusRef);

  WorkspaceContext timeRange(TimeRange? timeRange);

  WorkspaceContext asOf(DateTime? asOf);

  WorkspaceContext playback(WorkspaceContextPlayback? playback);

  WorkspaceContext viewport(WorkspaceViewport? viewport);

  WorkspaceContext activeSituationId(String? activeSituationId);

  WorkspaceContext activeHypothesisSetId(String? activeHypothesisSetId);

  WorkspaceContext filters(Map<String, Object>? filters);

  WorkspaceContext layers(List<String>? layers);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WorkspaceContext(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WorkspaceContext(...).copyWith(id: 12, name: "My name")
  /// ```
  WorkspaceContext call({
    List<String>? selectedRefs,
    String? focusRef,
    TimeRange? timeRange,
    DateTime? asOf,
    WorkspaceContextPlayback? playback,
    WorkspaceViewport? viewport,
    String? activeSituationId,
    String? activeHypothesisSetId,
    Map<String, Object>? filters,
    List<String>? layers,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfWorkspaceContext.copyWith(...)` or call `instanceOfWorkspaceContext.copyWith.fieldName(value)` for a single field.
class _$WorkspaceContextCWProxyImpl implements _$WorkspaceContextCWProxy {
  const _$WorkspaceContextCWProxyImpl(this._value);

  final WorkspaceContext _value;

  @override
  WorkspaceContext selectedRefs(List<String>? selectedRefs) =>
      call(selectedRefs: selectedRefs);

  @override
  WorkspaceContext focusRef(String? focusRef) => call(focusRef: focusRef);

  @override
  WorkspaceContext timeRange(TimeRange? timeRange) =>
      call(timeRange: timeRange);

  @override
  WorkspaceContext asOf(DateTime? asOf) => call(asOf: asOf);

  @override
  WorkspaceContext playback(WorkspaceContextPlayback? playback) =>
      call(playback: playback);

  @override
  WorkspaceContext viewport(WorkspaceViewport? viewport) =>
      call(viewport: viewport);

  @override
  WorkspaceContext activeSituationId(String? activeSituationId) =>
      call(activeSituationId: activeSituationId);

  @override
  WorkspaceContext activeHypothesisSetId(String? activeHypothesisSetId) =>
      call(activeHypothesisSetId: activeHypothesisSetId);

  @override
  WorkspaceContext filters(Map<String, Object>? filters) =>
      call(filters: filters);

  @override
  WorkspaceContext layers(List<String>? layers) => call(layers: layers);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WorkspaceContext(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WorkspaceContext(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  WorkspaceContext call({
    Object? selectedRefs = const $CopyWithPlaceholder(),
    Object? focusRef = const $CopyWithPlaceholder(),
    Object? timeRange = const $CopyWithPlaceholder(),
    Object? asOf = const $CopyWithPlaceholder(),
    Object? playback = const $CopyWithPlaceholder(),
    Object? viewport = const $CopyWithPlaceholder(),
    Object? activeSituationId = const $CopyWithPlaceholder(),
    Object? activeHypothesisSetId = const $CopyWithPlaceholder(),
    Object? filters = const $CopyWithPlaceholder(),
    Object? layers = const $CopyWithPlaceholder(),
  }) {
    return WorkspaceContext(
      selectedRefs: selectedRefs == const $CopyWithPlaceholder()
          ? _value.selectedRefs
          // ignore: cast_nullable_to_non_nullable
          : selectedRefs as List<String>?,
      focusRef: focusRef == const $CopyWithPlaceholder()
          ? _value.focusRef
          // ignore: cast_nullable_to_non_nullable
          : focusRef as String?,
      timeRange: timeRange == const $CopyWithPlaceholder()
          ? _value.timeRange
          // ignore: cast_nullable_to_non_nullable
          : timeRange as TimeRange?,
      asOf: asOf == const $CopyWithPlaceholder()
          ? _value.asOf
          // ignore: cast_nullable_to_non_nullable
          : asOf as DateTime?,
      playback: playback == const $CopyWithPlaceholder()
          ? _value.playback
          // ignore: cast_nullable_to_non_nullable
          : playback as WorkspaceContextPlayback?,
      viewport: viewport == const $CopyWithPlaceholder()
          ? _value.viewport
          // ignore: cast_nullable_to_non_nullable
          : viewport as WorkspaceViewport?,
      activeSituationId: activeSituationId == const $CopyWithPlaceholder()
          ? _value.activeSituationId
          // ignore: cast_nullable_to_non_nullable
          : activeSituationId as String?,
      activeHypothesisSetId:
          activeHypothesisSetId == const $CopyWithPlaceholder()
          ? _value.activeHypothesisSetId
          // ignore: cast_nullable_to_non_nullable
          : activeHypothesisSetId as String?,
      filters: filters == const $CopyWithPlaceholder()
          ? _value.filters
          // ignore: cast_nullable_to_non_nullable
          : filters as Map<String, Object>?,
      layers: layers == const $CopyWithPlaceholder()
          ? _value.layers
          // ignore: cast_nullable_to_non_nullable
          : layers as List<String>?,
    );
  }
}

extension $WorkspaceContextCopyWith on WorkspaceContext {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfWorkspaceContext.copyWith(...)` or `instanceOfWorkspaceContext.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$WorkspaceContextCWProxy get copyWith => _$WorkspaceContextCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

WorkspaceContext _$WorkspaceContextFromJson(Map<String, dynamic> json) =>
    $checkedCreate('WorkspaceContext', json, ($checkedConvert) {
      final val = WorkspaceContext(
        selectedRefs: $checkedConvert(
          'selectedRefs',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        focusRef: $checkedConvert('focusRef', (v) => v as String?),
        timeRange: $checkedConvert(
          'timeRange',
          (v) =>
              v == null ? null : TimeRange.fromJson(v as Map<String, dynamic>),
        ),
        asOf: $checkedConvert(
          'asOf',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        playback: $checkedConvert(
          'playback',
          (v) => v == null
              ? null
              : WorkspaceContextPlayback.fromJson(v as Map<String, dynamic>),
        ),
        viewport: $checkedConvert(
          'viewport',
          (v) => v == null
              ? null
              : WorkspaceViewport.fromJson(v as Map<String, dynamic>),
        ),
        activeSituationId: $checkedConvert(
          'activeSituationId',
          (v) => v as String?,
        ),
        activeHypothesisSetId: $checkedConvert(
          'activeHypothesisSetId',
          (v) => v as String?,
        ),
        filters: $checkedConvert(
          'filters',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, e as Object),
          ),
        ),
        layers: $checkedConvert(
          'layers',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$WorkspaceContextToJson(WorkspaceContext instance) =>
    <String, dynamic>{
      'selectedRefs': ?instance.selectedRefs,
      'focusRef': ?instance.focusRef,
      'timeRange': ?instance.timeRange?.toJson(),
      'asOf': ?instance.asOf?.toIso8601String(),
      'playback': ?instance.playback?.toJson(),
      'viewport': ?instance.viewport?.toJson(),
      'activeSituationId': ?instance.activeSituationId,
      'activeHypothesisSetId': ?instance.activeHypothesisSetId,
      'filters': ?instance.filters,
      'layers': ?instance.layers,
    };
