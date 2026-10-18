// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'hypothesis_revision.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$HypothesisRevisionCWProxy {
  HypothesisRevision id(String id);

  HypothesisRevision setId(String setId);

  HypothesisRevision at(DateTime at);

  HypothesisRevision cause(HypothesisRevisionCauseEnum cause);

  HypothesisRevision actor(String actor);

  HypothesisRevision asOfSeq(int asOfSeq);

  HypothesisRevision before(List<HypothesisProbabilityState> before);

  HypothesisRevision after(List<HypothesisProbabilityState> after);

  HypothesisRevision drivers(List<RevisionDriver> drivers);

  HypothesisRevision citations(List<String>? citations);

  HypothesisRevision rationale(String rationale);

  HypothesisRevision rationaleOrigin(
    HypothesisRevisionRationaleOriginEnum? rationaleOrigin,
  );

  HypothesisRevision bandChanged(bool? bandChanged);

  HypothesisRevision overrideValue(bool? overrideValue);

  HypothesisRevision ledgerForecastId(String? ledgerForecastId);

  HypothesisRevision updaterVersion(String updaterVersion);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HypothesisRevision(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HypothesisRevision(...).copyWith(id: 12, name: "My name")
  /// ```
  HypothesisRevision call({
    String id,
    String setId,
    DateTime at,
    HypothesisRevisionCauseEnum cause,
    String actor,
    int asOfSeq,
    List<HypothesisProbabilityState> before,
    List<HypothesisProbabilityState> after,
    List<RevisionDriver> drivers,
    List<String>? citations,
    String rationale,
    HypothesisRevisionRationaleOriginEnum? rationaleOrigin,
    bool? bandChanged,
    bool? overrideValue,
    String? ledgerForecastId,
    String updaterVersion,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfHypothesisRevision.copyWith(...)` or call `instanceOfHypothesisRevision.copyWith.fieldName(value)` for a single field.
class _$HypothesisRevisionCWProxyImpl implements _$HypothesisRevisionCWProxy {
  const _$HypothesisRevisionCWProxyImpl(this._value);

  final HypothesisRevision _value;

  @override
  HypothesisRevision id(String id) => call(id: id);

  @override
  HypothesisRevision setId(String setId) => call(setId: setId);

  @override
  HypothesisRevision at(DateTime at) => call(at: at);

  @override
  HypothesisRevision cause(HypothesisRevisionCauseEnum cause) =>
      call(cause: cause);

  @override
  HypothesisRevision actor(String actor) => call(actor: actor);

  @override
  HypothesisRevision asOfSeq(int asOfSeq) => call(asOfSeq: asOfSeq);

  @override
  HypothesisRevision before(List<HypothesisProbabilityState> before) =>
      call(before: before);

  @override
  HypothesisRevision after(List<HypothesisProbabilityState> after) =>
      call(after: after);

  @override
  HypothesisRevision drivers(List<RevisionDriver> drivers) =>
      call(drivers: drivers);

  @override
  HypothesisRevision citations(List<String>? citations) =>
      call(citations: citations);

  @override
  HypothesisRevision rationale(String rationale) => call(rationale: rationale);

  @override
  HypothesisRevision rationaleOrigin(
    HypothesisRevisionRationaleOriginEnum? rationaleOrigin,
  ) => call(rationaleOrigin: rationaleOrigin);

  @override
  HypothesisRevision bandChanged(bool? bandChanged) =>
      call(bandChanged: bandChanged);

  @override
  HypothesisRevision overrideValue(bool? overrideValue) =>
      call(overrideValue: overrideValue);

  @override
  HypothesisRevision ledgerForecastId(String? ledgerForecastId) =>
      call(ledgerForecastId: ledgerForecastId);

  @override
  HypothesisRevision updaterVersion(String updaterVersion) =>
      call(updaterVersion: updaterVersion);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HypothesisRevision(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HypothesisRevision(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  HypothesisRevision call({
    Object? id = const $CopyWithPlaceholder(),
    Object? setId = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
    Object? cause = const $CopyWithPlaceholder(),
    Object? actor = const $CopyWithPlaceholder(),
    Object? asOfSeq = const $CopyWithPlaceholder(),
    Object? before = const $CopyWithPlaceholder(),
    Object? after = const $CopyWithPlaceholder(),
    Object? drivers = const $CopyWithPlaceholder(),
    Object? citations = const $CopyWithPlaceholder(),
    Object? rationale = const $CopyWithPlaceholder(),
    Object? rationaleOrigin = const $CopyWithPlaceholder(),
    Object? bandChanged = const $CopyWithPlaceholder(),
    Object? overrideValue = const $CopyWithPlaceholder(),
    Object? ledgerForecastId = const $CopyWithPlaceholder(),
    Object? updaterVersion = const $CopyWithPlaceholder(),
  }) {
    return HypothesisRevision(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      setId: setId == const $CopyWithPlaceholder() || setId == null
          ? _value.setId
          // ignore: cast_nullable_to_non_nullable
          : setId as String,
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
      cause: cause == const $CopyWithPlaceholder() || cause == null
          ? _value.cause
          // ignore: cast_nullable_to_non_nullable
          : cause as HypothesisRevisionCauseEnum,
      actor: actor == const $CopyWithPlaceholder() || actor == null
          ? _value.actor
          // ignore: cast_nullable_to_non_nullable
          : actor as String,
      asOfSeq: asOfSeq == const $CopyWithPlaceholder() || asOfSeq == null
          ? _value.asOfSeq
          // ignore: cast_nullable_to_non_nullable
          : asOfSeq as int,
      before: before == const $CopyWithPlaceholder() || before == null
          ? _value.before
          // ignore: cast_nullable_to_non_nullable
          : before as List<HypothesisProbabilityState>,
      after: after == const $CopyWithPlaceholder() || after == null
          ? _value.after
          // ignore: cast_nullable_to_non_nullable
          : after as List<HypothesisProbabilityState>,
      drivers: drivers == const $CopyWithPlaceholder() || drivers == null
          ? _value.drivers
          // ignore: cast_nullable_to_non_nullable
          : drivers as List<RevisionDriver>,
      citations: citations == const $CopyWithPlaceholder()
          ? _value.citations
          // ignore: cast_nullable_to_non_nullable
          : citations as List<String>?,
      rationale: rationale == const $CopyWithPlaceholder() || rationale == null
          ? _value.rationale
          // ignore: cast_nullable_to_non_nullable
          : rationale as String,
      rationaleOrigin: rationaleOrigin == const $CopyWithPlaceholder()
          ? _value.rationaleOrigin
          // ignore: cast_nullable_to_non_nullable
          : rationaleOrigin as HypothesisRevisionRationaleOriginEnum?,
      bandChanged: bandChanged == const $CopyWithPlaceholder()
          ? _value.bandChanged
          // ignore: cast_nullable_to_non_nullable
          : bandChanged as bool?,
      overrideValue: overrideValue == const $CopyWithPlaceholder()
          ? _value.overrideValue
          // ignore: cast_nullable_to_non_nullable
          : overrideValue as bool?,
      ledgerForecastId: ledgerForecastId == const $CopyWithPlaceholder()
          ? _value.ledgerForecastId
          // ignore: cast_nullable_to_non_nullable
          : ledgerForecastId as String?,
      updaterVersion:
          updaterVersion == const $CopyWithPlaceholder() ||
              updaterVersion == null
          ? _value.updaterVersion
          // ignore: cast_nullable_to_non_nullable
          : updaterVersion as String,
    );
  }
}

extension $HypothesisRevisionCopyWith on HypothesisRevision {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfHypothesisRevision.copyWith(...)` or `instanceOfHypothesisRevision.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$HypothesisRevisionCWProxy get copyWith =>
      _$HypothesisRevisionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

HypothesisRevision _$HypothesisRevisionFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('HypothesisRevision', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'setId',
      'at',
      'cause',
      'actor',
      'asOfSeq',
      'before',
      'after',
      'drivers',
      'rationale',
      'updaterVersion',
    ],
  );
  final val = HypothesisRevision(
    id: $checkedConvert('id', (v) => v as String),
    setId: $checkedConvert('setId', (v) => v as String),
    at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
    cause: $checkedConvert(
      'cause',
      (v) => $enumDecode(_$HypothesisRevisionCauseEnumEnumMap, v),
    ),
    actor: $checkedConvert('actor', (v) => v as String),
    asOfSeq: $checkedConvert('asOfSeq', (v) => (v as num).toInt()),
    before: $checkedConvert(
      'before',
      (v) => (v as List<dynamic>)
          .map(
            (e) =>
                HypothesisProbabilityState.fromJson(e as Map<String, dynamic>),
          )
          .toList(),
    ),
    after: $checkedConvert(
      'after',
      (v) => (v as List<dynamic>)
          .map(
            (e) =>
                HypothesisProbabilityState.fromJson(e as Map<String, dynamic>),
          )
          .toList(),
    ),
    drivers: $checkedConvert(
      'drivers',
      (v) => (v as List<dynamic>)
          .map((e) => RevisionDriver.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    citations: $checkedConvert(
      'citations',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    rationale: $checkedConvert('rationale', (v) => v as String),
    rationaleOrigin: $checkedConvert(
      'rationaleOrigin',
      (v) => $enumDecodeNullable(
        _$HypothesisRevisionRationaleOriginEnumEnumMap,
        v,
      ),
    ),
    bandChanged: $checkedConvert('bandChanged', (v) => v as bool?),
    overrideValue: $checkedConvert('override', (v) => v as bool?),
    ledgerForecastId: $checkedConvert('ledgerForecastId', (v) => v as String?),
    updaterVersion: $checkedConvert('updaterVersion', (v) => v as String),
  );
  return val;
}, fieldKeyMap: const {'overrideValue': 'override'});

Map<String, dynamic> _$HypothesisRevisionToJson(
  HypothesisRevision instance,
) => <String, dynamic>{
  'id': instance.id,
  'setId': instance.setId,
  'at': instance.at.toIso8601String(),
  'cause': _$HypothesisRevisionCauseEnumEnumMap[instance.cause]!,
  'actor': instance.actor,
  'asOfSeq': instance.asOfSeq,
  'before': instance.before.map((e) => e.toJson()).toList(),
  'after': instance.after.map((e) => e.toJson()).toList(),
  'drivers': instance.drivers.map((e) => e.toJson()).toList(),
  'citations': ?instance.citations,
  'rationale': instance.rationale,
  'rationaleOrigin':
      ?_$HypothesisRevisionRationaleOriginEnumEnumMap[instance.rationaleOrigin],
  'bandChanged': ?instance.bandChanged,
  'override': ?instance.overrideValue,
  'ledgerForecastId': ?instance.ledgerForecastId,
  'updaterVersion': instance.updaterVersion,
};

const _$HypothesisRevisionCauseEnumEnumMap = {
  HypothesisRevisionCauseEnum.indicatorObserved: 'indicator-observed',
  HypothesisRevisionCauseEnum.indicatorAbsent: 'indicator-absent',
  HypothesisRevisionCauseEnum.evidenceAdded: 'evidence-added',
  HypothesisRevisionCauseEnum.evidenceRemoved: 'evidence-removed',
  HypothesisRevisionCauseEnum.baseRateRefresh: 'base-rate-refresh',
  HypothesisRevisionCauseEnum.review: 'review',
  HypothesisRevisionCauseEnum.operator_: 'operator',
  HypothesisRevisionCauseEnum.created: 'created',
};

const _$HypothesisRevisionRationaleOriginEnumEnumMap = {
  HypothesisRevisionRationaleOriginEnum.deterministic: 'deterministic',
  HypothesisRevisionRationaleOriginEnum.operator_: 'operator',
  HypothesisRevisionRationaleOriginEnum.analyst: 'analyst',
};
