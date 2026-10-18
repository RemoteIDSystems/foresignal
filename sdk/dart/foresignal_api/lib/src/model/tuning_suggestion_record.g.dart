// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'tuning_suggestion_record.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TuningSuggestionRecordCWProxy {
  TuningSuggestionRecord id(String id);

  TuningSuggestionRecord kind(TuningSuggestionRecordKindEnum kind);

  TuningSuggestionRecord rationale(String rationale);

  TuningSuggestionRecord patch_(Map<String, Object> patch_);

  TuningSuggestionRecord expectedEffect(
    TuningSuggestionExpectedEffect? expectedEffect,
  );

  TuningSuggestionRecord caveats(List<Caveat>? caveats);

  TuningSuggestionRecord ruleId(String ruleId);

  TuningSuggestionRecord ruleRev(String ruleRev);

  TuningSuggestionRecord status(TuningSuggestionRecordStatusEnum status);

  TuningSuggestionRecord createdAt(DateTime createdAt);

  TuningSuggestionRecord decidedBy(String? decidedBy);

  TuningSuggestionRecord decidedAt(DateTime? decidedAt);

  TuningSuggestionRecord decisionReason(String? decisionReason);

  TuningSuggestionRecord explanation(String? explanation);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TuningSuggestionRecord(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TuningSuggestionRecord(...).copyWith(id: 12, name: "My name")
  /// ```
  TuningSuggestionRecord call({
    String id,
    TuningSuggestionRecordKindEnum kind,
    String rationale,
    Map<String, Object> patch_,
    TuningSuggestionExpectedEffect? expectedEffect,
    List<Caveat>? caveats,
    String ruleId,
    String ruleRev,
    TuningSuggestionRecordStatusEnum status,
    DateTime createdAt,
    String? decidedBy,
    DateTime? decidedAt,
    String? decisionReason,
    String? explanation,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTuningSuggestionRecord.copyWith(...)` or call `instanceOfTuningSuggestionRecord.copyWith.fieldName(value)` for a single field.
class _$TuningSuggestionRecordCWProxyImpl
    implements _$TuningSuggestionRecordCWProxy {
  const _$TuningSuggestionRecordCWProxyImpl(this._value);

  final TuningSuggestionRecord _value;

  @override
  TuningSuggestionRecord id(String id) => call(id: id);

  @override
  TuningSuggestionRecord kind(TuningSuggestionRecordKindEnum kind) =>
      call(kind: kind);

  @override
  TuningSuggestionRecord rationale(String rationale) =>
      call(rationale: rationale);

  @override
  TuningSuggestionRecord patch_(Map<String, Object> patch_) =>
      call(patch_: patch_);

  @override
  TuningSuggestionRecord expectedEffect(
    TuningSuggestionExpectedEffect? expectedEffect,
  ) => call(expectedEffect: expectedEffect);

  @override
  TuningSuggestionRecord caveats(List<Caveat>? caveats) =>
      call(caveats: caveats);

  @override
  TuningSuggestionRecord ruleId(String ruleId) => call(ruleId: ruleId);

  @override
  TuningSuggestionRecord ruleRev(String ruleRev) => call(ruleRev: ruleRev);

  @override
  TuningSuggestionRecord status(TuningSuggestionRecordStatusEnum status) =>
      call(status: status);

  @override
  TuningSuggestionRecord createdAt(DateTime createdAt) =>
      call(createdAt: createdAt);

  @override
  TuningSuggestionRecord decidedBy(String? decidedBy) =>
      call(decidedBy: decidedBy);

  @override
  TuningSuggestionRecord decidedAt(DateTime? decidedAt) =>
      call(decidedAt: decidedAt);

  @override
  TuningSuggestionRecord decisionReason(String? decisionReason) =>
      call(decisionReason: decisionReason);

  @override
  TuningSuggestionRecord explanation(String? explanation) =>
      call(explanation: explanation);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TuningSuggestionRecord(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TuningSuggestionRecord(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TuningSuggestionRecord call({
    Object? id = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? rationale = const $CopyWithPlaceholder(),
    Object? patch_ = const $CopyWithPlaceholder(),
    Object? expectedEffect = const $CopyWithPlaceholder(),
    Object? caveats = const $CopyWithPlaceholder(),
    Object? ruleId = const $CopyWithPlaceholder(),
    Object? ruleRev = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? decidedBy = const $CopyWithPlaceholder(),
    Object? decidedAt = const $CopyWithPlaceholder(),
    Object? decisionReason = const $CopyWithPlaceholder(),
    Object? explanation = const $CopyWithPlaceholder(),
  }) {
    return TuningSuggestionRecord(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      kind: kind == const $CopyWithPlaceholder() || kind == null
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as TuningSuggestionRecordKindEnum,
      rationale: rationale == const $CopyWithPlaceholder() || rationale == null
          ? _value.rationale
          // ignore: cast_nullable_to_non_nullable
          : rationale as String,
      patch_: patch_ == const $CopyWithPlaceholder() || patch_ == null
          ? _value.patch_
          // ignore: cast_nullable_to_non_nullable
          : patch_ as Map<String, Object>,
      expectedEffect: expectedEffect == const $CopyWithPlaceholder()
          ? _value.expectedEffect
          // ignore: cast_nullable_to_non_nullable
          : expectedEffect as TuningSuggestionExpectedEffect?,
      caveats: caveats == const $CopyWithPlaceholder()
          ? _value.caveats
          // ignore: cast_nullable_to_non_nullable
          : caveats as List<Caveat>?,
      ruleId: ruleId == const $CopyWithPlaceholder() || ruleId == null
          ? _value.ruleId
          // ignore: cast_nullable_to_non_nullable
          : ruleId as String,
      ruleRev: ruleRev == const $CopyWithPlaceholder() || ruleRev == null
          ? _value.ruleRev
          // ignore: cast_nullable_to_non_nullable
          : ruleRev as String,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as TuningSuggestionRecordStatusEnum,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      decidedBy: decidedBy == const $CopyWithPlaceholder()
          ? _value.decidedBy
          // ignore: cast_nullable_to_non_nullable
          : decidedBy as String?,
      decidedAt: decidedAt == const $CopyWithPlaceholder()
          ? _value.decidedAt
          // ignore: cast_nullable_to_non_nullable
          : decidedAt as DateTime?,
      decisionReason: decisionReason == const $CopyWithPlaceholder()
          ? _value.decisionReason
          // ignore: cast_nullable_to_non_nullable
          : decisionReason as String?,
      explanation: explanation == const $CopyWithPlaceholder()
          ? _value.explanation
          // ignore: cast_nullable_to_non_nullable
          : explanation as String?,
    );
  }
}

extension $TuningSuggestionRecordCopyWith on TuningSuggestionRecord {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTuningSuggestionRecord.copyWith(...)` or `instanceOfTuningSuggestionRecord.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TuningSuggestionRecordCWProxy get copyWith =>
      _$TuningSuggestionRecordCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TuningSuggestionRecord _$TuningSuggestionRecordFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('TuningSuggestionRecord', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'kind',
      'rationale',
      'patch',
      'ruleId',
      'ruleRev',
      'status',
      'createdAt',
    ],
  );
  final val = TuningSuggestionRecord(
    id: $checkedConvert('id', (v) => v as String),
    kind: $checkedConvert(
      'kind',
      (v) => $enumDecode(_$TuningSuggestionRecordKindEnumEnumMap, v),
    ),
    rationale: $checkedConvert('rationale', (v) => v as String),
    patch_: $checkedConvert(
      'patch',
      (v) =>
          (v as Map<String, dynamic>).map((k, e) => MapEntry(k, e as Object)),
    ),
    expectedEffect: $checkedConvert(
      'expectedEffect',
      (v) => v == null
          ? null
          : TuningSuggestionExpectedEffect.fromJson(v as Map<String, dynamic>),
    ),
    caveats: $checkedConvert(
      'caveats',
      (v) => (v as List<dynamic>?)
          ?.map((e) => Caveat.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    ruleId: $checkedConvert('ruleId', (v) => v as String),
    ruleRev: $checkedConvert('ruleRev', (v) => v as String),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$TuningSuggestionRecordStatusEnumEnumMap, v),
    ),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    decidedBy: $checkedConvert('decidedBy', (v) => v as String?),
    decidedAt: $checkedConvert(
      'decidedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    decisionReason: $checkedConvert('decisionReason', (v) => v as String?),
    explanation: $checkedConvert('explanation', (v) => v as String?),
  );
  return val;
}, fieldKeyMap: const {'patch_': 'patch'});

Map<String, dynamic> _$TuningSuggestionRecordToJson(
  TuningSuggestionRecord instance,
) => <String, dynamic>{
  'id': instance.id,
  'kind': _$TuningSuggestionRecordKindEnumEnumMap[instance.kind]!,
  'rationale': instance.rationale,
  'patch': instance.patch_,
  'expectedEffect': ?instance.expectedEffect?.toJson(),
  'caveats': ?instance.caveats?.map((e) => e.toJson()).toList(),
  'ruleId': instance.ruleId,
  'ruleRev': instance.ruleRev,
  'status': _$TuningSuggestionRecordStatusEnumEnumMap[instance.status]!,
  'createdAt': instance.createdAt.toIso8601String(),
  'decidedBy': ?instance.decidedBy,
  'decidedAt': ?instance.decidedAt?.toIso8601String(),
  'decisionReason': ?instance.decisionReason,
  'explanation': ?instance.explanation,
};

const _$TuningSuggestionRecordKindEnumEnumMap = {
  TuningSuggestionRecordKindEnum.raiseThreshold: 'raise-threshold',
  TuningSuggestionRecordKindEnum.lowerThreshold: 'lower-threshold',
  TuningSuggestionRecordKindEnum.addCooldown: 'add-cooldown',
  TuningSuggestionRecordKindEnum.switchToEnter: 'switch-to-enter',
  TuningSuggestionRecordKindEnum.addEdgeMinInterval: 'add-edge-min-interval',
  TuningSuggestionRecordKindEnum.shrinkGeofence: 'shrink-geofence',
  TuningSuggestionRecordKindEnum.lowerAnomalySensitivity:
      'lower-anomaly-sensitivity',
  TuningSuggestionRecordKindEnum.ACTIVE_WINDOW: 'ACTIVE_WINDOW',
  TuningSuggestionRecordKindEnum.SEVERITY_UP: 'SEVERITY_UP',
  TuningSuggestionRecordKindEnum.SEVERITY_DOWN: 'SEVERITY_DOWN',
};

const _$TuningSuggestionRecordStatusEnumEnumMap = {
  TuningSuggestionRecordStatusEnum.OPEN: 'OPEN',
  TuningSuggestionRecordStatusEnum.APPLIED: 'APPLIED',
  TuningSuggestionRecordStatusEnum.DISMISSED: 'DISMISSED',
  TuningSuggestionRecordStatusEnum.STALE: 'STALE',
};
