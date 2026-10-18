// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'indicator_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IndicatorEventCWProxy {
  IndicatorEvent indicatorId(String indicatorId);

  IndicatorEvent setId(String setId);

  IndicatorEvent description(String? description);

  IndicatorEvent signalFamily(String? signalFamily);

  IndicatorEvent state(IndicatorEventStateEnum state);

  IndicatorEvent previousState(IndicatorEventPreviousStateEnum? previousState);

  IndicatorEvent evidence(Object? evidence);

  IndicatorEvent revisionId(String? revisionId);

  IndicatorEvent private(bool? private);

  IndicatorEvent at(DateTime at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IndicatorEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IndicatorEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  IndicatorEvent call({
    String indicatorId,
    String setId,
    String? description,
    String? signalFamily,
    IndicatorEventStateEnum state,
    IndicatorEventPreviousStateEnum? previousState,
    Object? evidence,
    String? revisionId,
    bool? private,
    DateTime at,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIndicatorEvent.copyWith(...)` or call `instanceOfIndicatorEvent.copyWith.fieldName(value)` for a single field.
class _$IndicatorEventCWProxyImpl implements _$IndicatorEventCWProxy {
  const _$IndicatorEventCWProxyImpl(this._value);

  final IndicatorEvent _value;

  @override
  IndicatorEvent indicatorId(String indicatorId) =>
      call(indicatorId: indicatorId);

  @override
  IndicatorEvent setId(String setId) => call(setId: setId);

  @override
  IndicatorEvent description(String? description) =>
      call(description: description);

  @override
  IndicatorEvent signalFamily(String? signalFamily) =>
      call(signalFamily: signalFamily);

  @override
  IndicatorEvent state(IndicatorEventStateEnum state) => call(state: state);

  @override
  IndicatorEvent previousState(
    IndicatorEventPreviousStateEnum? previousState,
  ) => call(previousState: previousState);

  @override
  IndicatorEvent evidence(Object? evidence) => call(evidence: evidence);

  @override
  IndicatorEvent revisionId(String? revisionId) => call(revisionId: revisionId);

  @override
  IndicatorEvent private(bool? private) => call(private: private);

  @override
  IndicatorEvent at(DateTime at) => call(at: at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IndicatorEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IndicatorEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IndicatorEvent call({
    Object? indicatorId = const $CopyWithPlaceholder(),
    Object? setId = const $CopyWithPlaceholder(),
    Object? description = const $CopyWithPlaceholder(),
    Object? signalFamily = const $CopyWithPlaceholder(),
    Object? state = const $CopyWithPlaceholder(),
    Object? previousState = const $CopyWithPlaceholder(),
    Object? evidence = const $CopyWithPlaceholder(),
    Object? revisionId = const $CopyWithPlaceholder(),
    Object? private = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
  }) {
    return IndicatorEvent(
      indicatorId:
          indicatorId == const $CopyWithPlaceholder() || indicatorId == null
          ? _value.indicatorId
          // ignore: cast_nullable_to_non_nullable
          : indicatorId as String,
      setId: setId == const $CopyWithPlaceholder() || setId == null
          ? _value.setId
          // ignore: cast_nullable_to_non_nullable
          : setId as String,
      description: description == const $CopyWithPlaceholder()
          ? _value.description
          // ignore: cast_nullable_to_non_nullable
          : description as String?,
      signalFamily: signalFamily == const $CopyWithPlaceholder()
          ? _value.signalFamily
          // ignore: cast_nullable_to_non_nullable
          : signalFamily as String?,
      state: state == const $CopyWithPlaceholder() || state == null
          ? _value.state
          // ignore: cast_nullable_to_non_nullable
          : state as IndicatorEventStateEnum,
      previousState: previousState == const $CopyWithPlaceholder()
          ? _value.previousState
          // ignore: cast_nullable_to_non_nullable
          : previousState as IndicatorEventPreviousStateEnum?,
      evidence: evidence == const $CopyWithPlaceholder()
          ? _value.evidence
          // ignore: cast_nullable_to_non_nullable
          : evidence as Object?,
      revisionId: revisionId == const $CopyWithPlaceholder()
          ? _value.revisionId
          // ignore: cast_nullable_to_non_nullable
          : revisionId as String?,
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

extension $IndicatorEventCopyWith on IndicatorEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIndicatorEvent.copyWith(...)` or `instanceOfIndicatorEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IndicatorEventCWProxy get copyWith => _$IndicatorEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IndicatorEvent _$IndicatorEventFromJson(Map<String, dynamic> json) =>
    $checkedCreate('IndicatorEvent', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['indicatorId', 'setId', 'state', 'at'],
      );
      final val = IndicatorEvent(
        indicatorId: $checkedConvert('indicatorId', (v) => v as String),
        setId: $checkedConvert('setId', (v) => v as String),
        description: $checkedConvert('description', (v) => v as String?),
        signalFamily: $checkedConvert('signalFamily', (v) => v as String?),
        state: $checkedConvert(
          'state',
          (v) => $enumDecode(_$IndicatorEventStateEnumEnumMap, v),
        ),
        previousState: $checkedConvert(
          'previousState',
          (v) =>
              $enumDecodeNullable(_$IndicatorEventPreviousStateEnumEnumMap, v),
        ),
        evidence: $checkedConvert('evidence', (v) => v),
        revisionId: $checkedConvert('revisionId', (v) => v as String?),
        private: $checkedConvert('private', (v) => v as bool?),
        at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
      );
      return val;
    });

Map<String, dynamic> _$IndicatorEventToJson(IndicatorEvent instance) =>
    <String, dynamic>{
      'indicatorId': instance.indicatorId,
      'setId': instance.setId,
      'description': ?instance.description,
      'signalFamily': ?instance.signalFamily,
      'state': _$IndicatorEventStateEnumEnumMap[instance.state]!,
      'previousState':
          ?_$IndicatorEventPreviousStateEnumEnumMap[instance.previousState],
      'evidence': ?instance.evidence,
      'revisionId': ?instance.revisionId,
      'private': ?instance.private,
      'at': instance.at.toIso8601String(),
    };

const _$IndicatorEventStateEnumEnumMap = {
  IndicatorEventStateEnum.pending: 'pending',
  IndicatorEventStateEnum.emerging: 'emerging',
  IndicatorEventStateEnum.observed: 'observed',
  IndicatorEventStateEnum.absent: 'absent',
  IndicatorEventStateEnum.unknownCoverage: 'unknown-coverage',
  IndicatorEventStateEnum.retired: 'retired',
  IndicatorEventStateEnum.reported: 'reported',
};

const _$IndicatorEventPreviousStateEnumEnumMap = {
  IndicatorEventPreviousStateEnum.pending: 'pending',
  IndicatorEventPreviousStateEnum.emerging: 'emerging',
  IndicatorEventPreviousStateEnum.observed: 'observed',
  IndicatorEventPreviousStateEnum.absent: 'absent',
  IndicatorEventPreviousStateEnum.unknownCoverage: 'unknown-coverage',
  IndicatorEventPreviousStateEnum.retired: 'retired',
  IndicatorEventPreviousStateEnum.reported: 'reported',
};
