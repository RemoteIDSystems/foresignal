// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'hypothesis_set_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$HypothesisSetEventCWProxy {
  HypothesisSetEvent setId(String setId);

  HypothesisSetEvent version(int version);

  HypothesisSetEvent question(String? question);

  HypothesisSetEvent status(HypothesisSetEventStatusEnum status);

  HypothesisSetEvent revisionId(String? revisionId);

  HypothesisSetEvent cause(HypothesisSetEventCauseEnum? cause);

  HypothesisSetEvent hypotheses(List<HypothesisProbabilityState>? hypotheses);

  HypothesisSetEvent bandChanges(
    List<HypothesisSetEventBandChangesInner>? bandChanges,
  );

  HypothesisSetEvent private(bool? private);

  HypothesisSetEvent at(DateTime at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HypothesisSetEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HypothesisSetEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  HypothesisSetEvent call({
    String setId,
    int version,
    String? question,
    HypothesisSetEventStatusEnum status,
    String? revisionId,
    HypothesisSetEventCauseEnum? cause,
    List<HypothesisProbabilityState>? hypotheses,
    List<HypothesisSetEventBandChangesInner>? bandChanges,
    bool? private,
    DateTime at,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfHypothesisSetEvent.copyWith(...)` or call `instanceOfHypothesisSetEvent.copyWith.fieldName(value)` for a single field.
class _$HypothesisSetEventCWProxyImpl implements _$HypothesisSetEventCWProxy {
  const _$HypothesisSetEventCWProxyImpl(this._value);

  final HypothesisSetEvent _value;

  @override
  HypothesisSetEvent setId(String setId) => call(setId: setId);

  @override
  HypothesisSetEvent version(int version) => call(version: version);

  @override
  HypothesisSetEvent question(String? question) => call(question: question);

  @override
  HypothesisSetEvent status(HypothesisSetEventStatusEnum status) =>
      call(status: status);

  @override
  HypothesisSetEvent revisionId(String? revisionId) =>
      call(revisionId: revisionId);

  @override
  HypothesisSetEvent cause(HypothesisSetEventCauseEnum? cause) =>
      call(cause: cause);

  @override
  HypothesisSetEvent hypotheses(List<HypothesisProbabilityState>? hypotheses) =>
      call(hypotheses: hypotheses);

  @override
  HypothesisSetEvent bandChanges(
    List<HypothesisSetEventBandChangesInner>? bandChanges,
  ) => call(bandChanges: bandChanges);

  @override
  HypothesisSetEvent private(bool? private) => call(private: private);

  @override
  HypothesisSetEvent at(DateTime at) => call(at: at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HypothesisSetEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HypothesisSetEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  HypothesisSetEvent call({
    Object? setId = const $CopyWithPlaceholder(),
    Object? version = const $CopyWithPlaceholder(),
    Object? question = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? revisionId = const $CopyWithPlaceholder(),
    Object? cause = const $CopyWithPlaceholder(),
    Object? hypotheses = const $CopyWithPlaceholder(),
    Object? bandChanges = const $CopyWithPlaceholder(),
    Object? private = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
  }) {
    return HypothesisSetEvent(
      setId: setId == const $CopyWithPlaceholder() || setId == null
          ? _value.setId
          // ignore: cast_nullable_to_non_nullable
          : setId as String,
      version: version == const $CopyWithPlaceholder() || version == null
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as int,
      question: question == const $CopyWithPlaceholder()
          ? _value.question
          // ignore: cast_nullable_to_non_nullable
          : question as String?,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as HypothesisSetEventStatusEnum,
      revisionId: revisionId == const $CopyWithPlaceholder()
          ? _value.revisionId
          // ignore: cast_nullable_to_non_nullable
          : revisionId as String?,
      cause: cause == const $CopyWithPlaceholder()
          ? _value.cause
          // ignore: cast_nullable_to_non_nullable
          : cause as HypothesisSetEventCauseEnum?,
      hypotheses: hypotheses == const $CopyWithPlaceholder()
          ? _value.hypotheses
          // ignore: cast_nullable_to_non_nullable
          : hypotheses as List<HypothesisProbabilityState>?,
      bandChanges: bandChanges == const $CopyWithPlaceholder()
          ? _value.bandChanges
          // ignore: cast_nullable_to_non_nullable
          : bandChanges as List<HypothesisSetEventBandChangesInner>?,
      private: private == const $CopyWithPlaceholder()
          ? _value.private
          // ignore: cast_nullable_to_non_nullable
          : private as bool?,
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
    );
  }
}

extension $HypothesisSetEventCopyWith on HypothesisSetEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfHypothesisSetEvent.copyWith(...)` or `instanceOfHypothesisSetEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$HypothesisSetEventCWProxy get copyWith =>
      _$HypothesisSetEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

HypothesisSetEvent _$HypothesisSetEventFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('HypothesisSetEvent', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['setId', 'version', 'status', 'at']);
  final val = HypothesisSetEvent(
    setId: $checkedConvert('setId', (v) => v as String),
    version: $checkedConvert('version', (v) => (v as num).toInt()),
    question: $checkedConvert('question', (v) => v as String?),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$HypothesisSetEventStatusEnumEnumMap, v),
    ),
    revisionId: $checkedConvert('revisionId', (v) => v as String?),
    cause: $checkedConvert(
      'cause',
      (v) => $enumDecodeNullable(_$HypothesisSetEventCauseEnumEnumMap, v),
    ),
    hypotheses: $checkedConvert(
      'hypotheses',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) =>
                HypothesisProbabilityState.fromJson(e as Map<String, dynamic>),
          )
          .toList(),
    ),
    bandChanges: $checkedConvert(
      'bandChanges',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => HypothesisSetEventBandChangesInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    private: $checkedConvert('private', (v) => v as bool?),
    at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
  );
  return val;
});

Map<String, dynamic> _$HypothesisSetEventToJson(HypothesisSetEvent instance) =>
    <String, dynamic>{
      'setId': instance.setId,
      'version': instance.version,
      'question': ?instance.question,
      'status': _$HypothesisSetEventStatusEnumEnumMap[instance.status]!,
      'revisionId': ?instance.revisionId,
      'cause': ?_$HypothesisSetEventCauseEnumEnumMap[instance.cause],
      'hypotheses': ?instance.hypotheses?.map((e) => e.toJson()).toList(),
      'bandChanges': ?instance.bandChanges?.map((e) => e.toJson()).toList(),
      'private': ?instance.private,
      'at': instance.at.toIso8601String(),
    };

const _$HypothesisSetEventStatusEnumEnumMap = {
  HypothesisSetEventStatusEnum.active: 'active',
  HypothesisSetEventStatusEnum.resolved: 'resolved',
  HypothesisSetEventStatusEnum.archived: 'archived',
};

const _$HypothesisSetEventCauseEnumEnumMap = {
  HypothesisSetEventCauseEnum.indicatorObserved: 'indicator-observed',
  HypothesisSetEventCauseEnum.indicatorAbsent: 'indicator-absent',
  HypothesisSetEventCauseEnum.evidenceAdded: 'evidence-added',
  HypothesisSetEventCauseEnum.evidenceRemoved: 'evidence-removed',
  HypothesisSetEventCauseEnum.baseRateRefresh: 'base-rate-refresh',
  HypothesisSetEventCauseEnum.review: 'review',
  HypothesisSetEventCauseEnum.operator_: 'operator',
  HypothesisSetEventCauseEnum.created: 'created',
};
