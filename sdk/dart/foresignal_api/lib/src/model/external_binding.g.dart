// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'external_binding.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ExternalBindingCWProxy {
  ExternalBinding streamId(String streamId);

  ExternalBinding target(ExternalBindingTargetEnum target);

  ExternalBinding config(Map<String, Object>? config);

  ExternalBinding filter(List<Map<String, Object>>? filter);

  ExternalBinding enabled(bool? enabled);

  ExternalBinding backfill(bool? backfill);

  ExternalBinding id(String id);

  ExternalBinding sourceId(String sourceId);

  ExternalBinding manifestSha256(String? manifestSha256);

  ExternalBinding state(ExternalBindingStateEnum state);

  ExternalBinding applied(int? applied);

  ExternalBinding lastError(String? lastError);

  ExternalBinding createdBy(String? createdBy);

  ExternalBinding createdAt(DateTime createdAt);

  ExternalBinding targetRef(String? targetRef);

  ExternalBinding updatedAt(DateTime? updatedAt);

  ExternalBinding lastAppliedAt(DateTime? lastAppliedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExternalBinding(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExternalBinding(...).copyWith(id: 12, name: "My name")
  /// ```
  ExternalBinding call({
    String streamId,
    ExternalBindingTargetEnum target,
    Map<String, Object>? config,
    List<Map<String, Object>>? filter,
    bool? enabled,
    bool? backfill,
    String id,
    String sourceId,
    String? manifestSha256,
    ExternalBindingStateEnum state,
    int? applied,
    String? lastError,
    String? createdBy,
    DateTime createdAt,
    String? targetRef,
    DateTime? updatedAt,
    DateTime? lastAppliedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfExternalBinding.copyWith(...)` or call `instanceOfExternalBinding.copyWith.fieldName(value)` for a single field.
class _$ExternalBindingCWProxyImpl implements _$ExternalBindingCWProxy {
  const _$ExternalBindingCWProxyImpl(this._value);

  final ExternalBinding _value;

  @override
  ExternalBinding streamId(String streamId) => call(streamId: streamId);

  @override
  ExternalBinding target(ExternalBindingTargetEnum target) =>
      call(target: target);

  @override
  ExternalBinding config(Map<String, Object>? config) => call(config: config);

  @override
  ExternalBinding filter(List<Map<String, Object>>? filter) =>
      call(filter: filter);

  @override
  ExternalBinding enabled(bool? enabled) => call(enabled: enabled);

  @override
  ExternalBinding backfill(bool? backfill) => call(backfill: backfill);

  @override
  ExternalBinding id(String id) => call(id: id);

  @override
  ExternalBinding sourceId(String sourceId) => call(sourceId: sourceId);

  @override
  ExternalBinding manifestSha256(String? manifestSha256) =>
      call(manifestSha256: manifestSha256);

  @override
  ExternalBinding state(ExternalBindingStateEnum state) => call(state: state);

  @override
  ExternalBinding applied(int? applied) => call(applied: applied);

  @override
  ExternalBinding lastError(String? lastError) => call(lastError: lastError);

  @override
  ExternalBinding createdBy(String? createdBy) => call(createdBy: createdBy);

  @override
  ExternalBinding createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  ExternalBinding targetRef(String? targetRef) => call(targetRef: targetRef);

  @override
  ExternalBinding updatedAt(DateTime? updatedAt) => call(updatedAt: updatedAt);

  @override
  ExternalBinding lastAppliedAt(DateTime? lastAppliedAt) =>
      call(lastAppliedAt: lastAppliedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExternalBinding(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExternalBinding(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ExternalBinding call({
    Object? streamId = const $CopyWithPlaceholder(),
    Object? target = const $CopyWithPlaceholder(),
    Object? config = const $CopyWithPlaceholder(),
    Object? filter = const $CopyWithPlaceholder(),
    Object? enabled = const $CopyWithPlaceholder(),
    Object? backfill = const $CopyWithPlaceholder(),
    Object? id = const $CopyWithPlaceholder(),
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? manifestSha256 = const $CopyWithPlaceholder(),
    Object? state = const $CopyWithPlaceholder(),
    Object? applied = const $CopyWithPlaceholder(),
    Object? lastError = const $CopyWithPlaceholder(),
    Object? createdBy = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? targetRef = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
    Object? lastAppliedAt = const $CopyWithPlaceholder(),
  }) {
    return ExternalBinding(
      streamId: streamId == const $CopyWithPlaceholder() || streamId == null
          ? _value.streamId
          // ignore: cast_nullable_to_non_nullable
          : streamId as String,
      target: target == const $CopyWithPlaceholder() || target == null
          ? _value.target
          // ignore: cast_nullable_to_non_nullable
          : target as ExternalBindingTargetEnum,
      config: config == const $CopyWithPlaceholder()
          ? _value.config
          // ignore: cast_nullable_to_non_nullable
          : config as Map<String, Object>?,
      filter: filter == const $CopyWithPlaceholder()
          ? _value.filter
          // ignore: cast_nullable_to_non_nullable
          : filter as List<Map<String, Object>>?,
      enabled: enabled == const $CopyWithPlaceholder()
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool?,
      backfill: backfill == const $CopyWithPlaceholder()
          ? _value.backfill
          // ignore: cast_nullable_to_non_nullable
          : backfill as bool?,
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      sourceId: sourceId == const $CopyWithPlaceholder() || sourceId == null
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String,
      manifestSha256: manifestSha256 == const $CopyWithPlaceholder()
          ? _value.manifestSha256
          // ignore: cast_nullable_to_non_nullable
          : manifestSha256 as String?,
      state: state == const $CopyWithPlaceholder() || state == null
          ? _value.state
          // ignore: cast_nullable_to_non_nullable
          : state as ExternalBindingStateEnum,
      applied: applied == const $CopyWithPlaceholder()
          ? _value.applied
          // ignore: cast_nullable_to_non_nullable
          : applied as int?,
      lastError: lastError == const $CopyWithPlaceholder()
          ? _value.lastError
          // ignore: cast_nullable_to_non_nullable
          : lastError as String?,
      createdBy: createdBy == const $CopyWithPlaceholder()
          ? _value.createdBy
          // ignore: cast_nullable_to_non_nullable
          : createdBy as String?,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      targetRef: targetRef == const $CopyWithPlaceholder()
          ? _value.targetRef
          // ignore: cast_nullable_to_non_nullable
          : targetRef as String?,
      updatedAt: updatedAt == const $CopyWithPlaceholder()
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime?,
      lastAppliedAt: lastAppliedAt == const $CopyWithPlaceholder()
          ? _value.lastAppliedAt
          // ignore: cast_nullable_to_non_nullable
          : lastAppliedAt as DateTime?,
    );
  }
}

extension $ExternalBindingCopyWith on ExternalBinding {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfExternalBinding.copyWith(...)` or `instanceOfExternalBinding.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ExternalBindingCWProxy get copyWith => _$ExternalBindingCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ExternalBinding _$ExternalBindingFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ExternalBinding', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'streamId',
      'target',
      'id',
      'sourceId',
      'state',
      'createdAt',
    ],
  );
  final val = ExternalBinding(
    streamId: $checkedConvert('streamId', (v) => v as String),
    target: $checkedConvert(
      'target',
      (v) => $enumDecode(_$ExternalBindingTargetEnumEnumMap, v),
    ),
    config: $checkedConvert(
      'config',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
    filter: $checkedConvert(
      'filter',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => (e as Map<String, dynamic>).map(
              (k, e) => MapEntry(k, e as Object),
            ),
          )
          .toList(),
    ),
    enabled: $checkedConvert('enabled', (v) => v as bool? ?? true),
    backfill: $checkedConvert('backfill', (v) => v as bool? ?? true),
    id: $checkedConvert('id', (v) => v as String),
    sourceId: $checkedConvert('sourceId', (v) => v as String),
    manifestSha256: $checkedConvert('manifestSha256', (v) => v as String?),
    state: $checkedConvert(
      'state',
      (v) => $enumDecode(_$ExternalBindingStateEnumEnumMap, v),
    ),
    applied: $checkedConvert('applied', (v) => (v as num?)?.toInt()),
    lastError: $checkedConvert('lastError', (v) => v as String?),
    createdBy: $checkedConvert('createdBy', (v) => v as String?),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    targetRef: $checkedConvert('targetRef', (v) => v as String?),
    updatedAt: $checkedConvert(
      'updatedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    lastAppliedAt: $checkedConvert(
      'lastAppliedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
  );
  return val;
});

Map<String, dynamic> _$ExternalBindingToJson(ExternalBinding instance) =>
    <String, dynamic>{
      'streamId': instance.streamId,
      'target': _$ExternalBindingTargetEnumEnumMap[instance.target]!,
      'config': ?instance.config,
      'filter': ?instance.filter,
      'enabled': ?instance.enabled,
      'backfill': ?instance.backfill,
      'id': instance.id,
      'sourceId': instance.sourceId,
      'manifestSha256': ?instance.manifestSha256,
      'state': _$ExternalBindingStateEnumEnumMap[instance.state]!,
      'applied': ?instance.applied,
      'lastError': ?instance.lastError,
      'createdBy': ?instance.createdBy,
      'createdAt': instance.createdAt.toIso8601String(),
      'targetRef': ?instance.targetRef,
      'updatedAt': ?instance.updatedAt?.toIso8601String(),
      'lastAppliedAt': ?instance.lastAppliedAt?.toIso8601String(),
    };

const _$ExternalBindingTargetEnumEnumMap = {
  ExternalBindingTargetEnum.series: 'series',
  ExternalBindingTargetEnum.canonicalFamily: 'canonical-family',
  ExternalBindingTargetEnum.social: 'social',
  ExternalBindingTargetEnum.news: 'news',
  ExternalBindingTargetEnum.evidence: 'evidence',
  ExternalBindingTargetEnum.indicator: 'indicator',
  ExternalBindingTargetEnum.forecast: 'forecast',
  ExternalBindingTargetEnum.place: 'place',
  ExternalBindingTargetEnum.situation: 'situation',
  ExternalBindingTargetEnum.chronicle: 'chronicle',
};

const _$ExternalBindingStateEnumEnumMap = {
  ExternalBindingStateEnum.active: 'active',
  ExternalBindingStateEnum.disabled: 'disabled',
  ExternalBindingStateEnum.backfilling: 'backfilling',
  ExternalBindingStateEnum.error: 'error',
};
