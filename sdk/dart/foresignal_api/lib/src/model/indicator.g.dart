// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'indicator.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IndicatorCWProxy {
  Indicator id(String id);

  Indicator setId(String setId);

  Indicator description(String description);

  Indicator signal(IndicatorSignal signal);

  Indicator signalFamily(String signalFamily);

  Indicator credibility(IndicatorCredibilityEnum credibility);

  Indicator relevance(IndicatorRelevanceEnum relevance);

  Indicator expectedBy(DateTime? expectedBy);

  Indicator lr(Map<String, IndicatorLikelihood> lr);

  Indicator absenceLr(Map<String, num>? absenceLr);

  Indicator state(IndicatorStateEnum state);

  Indicator watchRuleId(String? watchRuleId);

  Indicator pDetect(num? pDetect);

  Indicator observedEvidence(Map<String, Object>? observedEvidence);

  Indicator lastStateChangeAt(DateTime? lastStateChangeAt);

  Indicator origin(IndicatorOriginEnum origin);

  Indicator version(int? version);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Indicator(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Indicator(...).copyWith(id: 12, name: "My name")
  /// ```
  Indicator call({
    String id,
    String setId,
    String description,
    IndicatorSignal signal,
    String signalFamily,
    IndicatorCredibilityEnum credibility,
    IndicatorRelevanceEnum relevance,
    DateTime? expectedBy,
    Map<String, IndicatorLikelihood> lr,
    Map<String, num>? absenceLr,
    IndicatorStateEnum state,
    String? watchRuleId,
    num? pDetect,
    Map<String, Object>? observedEvidence,
    DateTime? lastStateChangeAt,
    IndicatorOriginEnum origin,
    int? version,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIndicator.copyWith(...)` or call `instanceOfIndicator.copyWith.fieldName(value)` for a single field.
class _$IndicatorCWProxyImpl implements _$IndicatorCWProxy {
  const _$IndicatorCWProxyImpl(this._value);

  final Indicator _value;

  @override
  Indicator id(String id) => call(id: id);

  @override
  Indicator setId(String setId) => call(setId: setId);

  @override
  Indicator description(String description) => call(description: description);

  @override
  Indicator signal(IndicatorSignal signal) => call(signal: signal);

  @override
  Indicator signalFamily(String signalFamily) =>
      call(signalFamily: signalFamily);

  @override
  Indicator credibility(IndicatorCredibilityEnum credibility) =>
      call(credibility: credibility);

  @override
  Indicator relevance(IndicatorRelevanceEnum relevance) =>
      call(relevance: relevance);

  @override
  Indicator expectedBy(DateTime? expectedBy) => call(expectedBy: expectedBy);

  @override
  Indicator lr(Map<String, IndicatorLikelihood> lr) => call(lr: lr);

  @override
  Indicator absenceLr(Map<String, num>? absenceLr) =>
      call(absenceLr: absenceLr);

  @override
  Indicator state(IndicatorStateEnum state) => call(state: state);

  @override
  Indicator watchRuleId(String? watchRuleId) => call(watchRuleId: watchRuleId);

  @override
  Indicator pDetect(num? pDetect) => call(pDetect: pDetect);

  @override
  Indicator observedEvidence(Map<String, Object>? observedEvidence) =>
      call(observedEvidence: observedEvidence);

  @override
  Indicator lastStateChangeAt(DateTime? lastStateChangeAt) =>
      call(lastStateChangeAt: lastStateChangeAt);

  @override
  Indicator origin(IndicatorOriginEnum origin) => call(origin: origin);

  @override
  Indicator version(int? version) => call(version: version);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Indicator(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Indicator(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Indicator call({
    Object? id = const $CopyWithPlaceholder(),
    Object? setId = const $CopyWithPlaceholder(),
    Object? description = const $CopyWithPlaceholder(),
    Object? signal = const $CopyWithPlaceholder(),
    Object? signalFamily = const $CopyWithPlaceholder(),
    Object? credibility = const $CopyWithPlaceholder(),
    Object? relevance = const $CopyWithPlaceholder(),
    Object? expectedBy = const $CopyWithPlaceholder(),
    Object? lr = const $CopyWithPlaceholder(),
    Object? absenceLr = const $CopyWithPlaceholder(),
    Object? state = const $CopyWithPlaceholder(),
    Object? watchRuleId = const $CopyWithPlaceholder(),
    Object? pDetect = const $CopyWithPlaceholder(),
    Object? observedEvidence = const $CopyWithPlaceholder(),
    Object? lastStateChangeAt = const $CopyWithPlaceholder(),
    Object? origin = const $CopyWithPlaceholder(),
    Object? version = const $CopyWithPlaceholder(),
  }) {
    return Indicator(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      setId: setId == const $CopyWithPlaceholder() || setId == null
          ? _value.setId
          // ignore: cast_nullable_to_non_nullable
          : setId as String,
      description:
          description == const $CopyWithPlaceholder() || description == null
          ? _value.description
          // ignore: cast_nullable_to_non_nullable
          : description as String,
      signal: signal == const $CopyWithPlaceholder() || signal == null
          ? _value.signal
          // ignore: cast_nullable_to_non_nullable
          : signal as IndicatorSignal,
      signalFamily:
          signalFamily == const $CopyWithPlaceholder() || signalFamily == null
          ? _value.signalFamily
          // ignore: cast_nullable_to_non_nullable
          : signalFamily as String,
      credibility:
          credibility == const $CopyWithPlaceholder() || credibility == null
          ? _value.credibility
          // ignore: cast_nullable_to_non_nullable
          : credibility as IndicatorCredibilityEnum,
      relevance: relevance == const $CopyWithPlaceholder() || relevance == null
          ? _value.relevance
          // ignore: cast_nullable_to_non_nullable
          : relevance as IndicatorRelevanceEnum,
      expectedBy: expectedBy == const $CopyWithPlaceholder()
          ? _value.expectedBy
          // ignore: cast_nullable_to_non_nullable
          : expectedBy as DateTime?,
      lr: lr == const $CopyWithPlaceholder() || lr == null
          ? _value.lr
          // ignore: cast_nullable_to_non_nullable
          : lr as Map<String, IndicatorLikelihood>,
      absenceLr: absenceLr == const $CopyWithPlaceholder()
          ? _value.absenceLr
          // ignore: cast_nullable_to_non_nullable
          : absenceLr as Map<String, num>?,
      state: state == const $CopyWithPlaceholder() || state == null
          ? _value.state
          // ignore: cast_nullable_to_non_nullable
          : state as IndicatorStateEnum,
      watchRuleId: watchRuleId == const $CopyWithPlaceholder()
          ? _value.watchRuleId
          // ignore: cast_nullable_to_non_nullable
          : watchRuleId as String?,
      pDetect: pDetect == const $CopyWithPlaceholder()
          ? _value.pDetect
          // ignore: cast_nullable_to_non_nullable
          : pDetect as num?,
      observedEvidence: observedEvidence == const $CopyWithPlaceholder()
          ? _value.observedEvidence
          // ignore: cast_nullable_to_non_nullable
          : observedEvidence as Map<String, Object>?,
      lastStateChangeAt: lastStateChangeAt == const $CopyWithPlaceholder()
          ? _value.lastStateChangeAt
          // ignore: cast_nullable_to_non_nullable
          : lastStateChangeAt as DateTime?,
      origin: origin == const $CopyWithPlaceholder() || origin == null
          ? _value.origin
          // ignore: cast_nullable_to_non_nullable
          : origin as IndicatorOriginEnum,
      version: version == const $CopyWithPlaceholder()
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as int?,
    );
  }
}

extension $IndicatorCopyWith on Indicator {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIndicator.copyWith(...)` or `instanceOfIndicator.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IndicatorCWProxy get copyWith => _$IndicatorCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Indicator _$IndicatorFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('Indicator', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'setId',
      'description',
      'signal',
      'signalFamily',
      'credibility',
      'relevance',
      'lr',
      'state',
      'origin',
    ],
  );
  final val = Indicator(
    id: $checkedConvert('id', (v) => v as String),
    setId: $checkedConvert('setId', (v) => v as String),
    description: $checkedConvert('description', (v) => v as String),
    signal: $checkedConvert(
      'signal',
      (v) => IndicatorSignal.fromJson(v as Map<String, dynamic>),
    ),
    signalFamily: $checkedConvert('signalFamily', (v) => v as String),
    credibility: $checkedConvert(
      'credibility',
      (v) => $enumDecode(_$IndicatorCredibilityEnumEnumMap, v),
    ),
    relevance: $checkedConvert(
      'relevance',
      (v) => $enumDecode(_$IndicatorRelevanceEnumEnumMap, v),
    ),
    expectedBy: $checkedConvert(
      'expectedBy',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    lr: $checkedConvert(
      'lr',
      (v) => (v as Map<String, dynamic>).map(
        (k, e) => MapEntry(
          k,
          IndicatorLikelihood.fromJson(e as Map<String, dynamic>),
        ),
      ),
    ),
    absenceLr: $checkedConvert(
      'absenceLr',
      (v) => (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as num)),
    ),
    state: $checkedConvert(
      'state',
      (v) => $enumDecode(_$IndicatorStateEnumEnumMap, v),
    ),
    watchRuleId: $checkedConvert('watchRuleId', (v) => v as String?),
    pDetect: $checkedConvert('pDetect', (v) => v as num?),
    observedEvidence: $checkedConvert(
      'observedEvidence',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
    lastStateChangeAt: $checkedConvert(
      'lastStateChangeAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    origin: $checkedConvert(
      'origin',
      (v) => $enumDecode(_$IndicatorOriginEnumEnumMap, v),
    ),
    version: $checkedConvert('version', (v) => (v as num?)?.toInt()),
  );
  return val;
});

Map<String, dynamic> _$IndicatorToJson(Indicator instance) => <String, dynamic>{
  'id': instance.id,
  'setId': instance.setId,
  'description': instance.description,
  'signal': instance.signal.toJson(),
  'signalFamily': instance.signalFamily,
  'credibility': _$IndicatorCredibilityEnumEnumMap[instance.credibility]!,
  'relevance': _$IndicatorRelevanceEnumEnumMap[instance.relevance]!,
  'expectedBy': ?instance.expectedBy?.toIso8601String(),
  'lr': instance.lr.map((k, e) => MapEntry(k, e.toJson())),
  'absenceLr': ?instance.absenceLr,
  'state': _$IndicatorStateEnumEnumMap[instance.state]!,
  'watchRuleId': ?instance.watchRuleId,
  'pDetect': ?instance.pDetect,
  'observedEvidence': ?instance.observedEvidence,
  'lastStateChangeAt': ?instance.lastStateChangeAt?.toIso8601String(),
  'origin': _$IndicatorOriginEnumEnumMap[instance.origin]!,
  'version': ?instance.version,
};

const _$IndicatorCredibilityEnumEnumMap = {
  IndicatorCredibilityEnum.H: 'H',
  IndicatorCredibilityEnum.M: 'M',
  IndicatorCredibilityEnum.L: 'L',
};

const _$IndicatorRelevanceEnumEnumMap = {
  IndicatorRelevanceEnum.H: 'H',
  IndicatorRelevanceEnum.M: 'M',
  IndicatorRelevanceEnum.L: 'L',
};

const _$IndicatorStateEnumEnumMap = {
  IndicatorStateEnum.pending: 'pending',
  IndicatorStateEnum.emerging: 'emerging',
  IndicatorStateEnum.observed: 'observed',
  IndicatorStateEnum.absent: 'absent',
  IndicatorStateEnum.unknownCoverage: 'unknown-coverage',
  IndicatorStateEnum.retired: 'retired',
  IndicatorStateEnum.reported: 'reported',
};

const _$IndicatorOriginEnumEnumMap = {
  IndicatorOriginEnum.operator_: 'operator',
  IndicatorOriginEnum.agent: 'agent',
  IndicatorOriginEnum.analyst: 'analyst',
};
