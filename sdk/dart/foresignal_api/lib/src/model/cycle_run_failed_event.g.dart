// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'cycle_run_failed_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CycleRunFailedEventCWProxy {
  CycleRunFailedEvent runId(String runId);

  CycleRunFailedEvent cycleId(String? cycleId);

  CycleRunFailedEvent situationId(String? situationId);

  CycleRunFailedEvent cadence(CycleRunFailedEventCadenceEnum cadence);

  CycleRunFailedEvent reason(String reason);

  CycleRunFailedEvent at(DateTime at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CycleRunFailedEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CycleRunFailedEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  CycleRunFailedEvent call({
    String runId,
    String? cycleId,
    String? situationId,
    CycleRunFailedEventCadenceEnum cadence,
    String reason,
    DateTime at,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCycleRunFailedEvent.copyWith(...)` or call `instanceOfCycleRunFailedEvent.copyWith.fieldName(value)` for a single field.
class _$CycleRunFailedEventCWProxyImpl implements _$CycleRunFailedEventCWProxy {
  const _$CycleRunFailedEventCWProxyImpl(this._value);

  final CycleRunFailedEvent _value;

  @override
  CycleRunFailedEvent runId(String runId) => call(runId: runId);

  @override
  CycleRunFailedEvent cycleId(String? cycleId) => call(cycleId: cycleId);

  @override
  CycleRunFailedEvent situationId(String? situationId) =>
      call(situationId: situationId);

  @override
  CycleRunFailedEvent cadence(CycleRunFailedEventCadenceEnum cadence) =>
      call(cadence: cadence);

  @override
  CycleRunFailedEvent reason(String reason) => call(reason: reason);

  @override
  CycleRunFailedEvent at(DateTime at) => call(at: at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CycleRunFailedEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CycleRunFailedEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CycleRunFailedEvent call({
    Object? runId = const $CopyWithPlaceholder(),
    Object? cycleId = const $CopyWithPlaceholder(),
    Object? situationId = const $CopyWithPlaceholder(),
    Object? cadence = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
  }) {
    return CycleRunFailedEvent(
      runId: runId == const $CopyWithPlaceholder() || runId == null
          ? _value.runId
          // ignore: cast_nullable_to_non_nullable
          : runId as String,
      cycleId: cycleId == const $CopyWithPlaceholder()
          ? _value.cycleId
          // ignore: cast_nullable_to_non_nullable
          : cycleId as String?,
      situationId: situationId == const $CopyWithPlaceholder()
          ? _value.situationId
          // ignore: cast_nullable_to_non_nullable
          : situationId as String?,
      cadence: cadence == const $CopyWithPlaceholder() || cadence == null
          ? _value.cadence
          // ignore: cast_nullable_to_non_nullable
          : cadence as CycleRunFailedEventCadenceEnum,
      reason: reason == const $CopyWithPlaceholder() || reason == null
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as String,
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
    );
  }
}

extension $CycleRunFailedEventCopyWith on CycleRunFailedEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCycleRunFailedEvent.copyWith(...)` or `instanceOfCycleRunFailedEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CycleRunFailedEventCWProxy get copyWith =>
      _$CycleRunFailedEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CycleRunFailedEvent _$CycleRunFailedEventFromJson(Map<String, dynamic> json) =>
    $checkedCreate('CycleRunFailedEvent', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['runId', 'cadence', 'reason', 'at'],
      );
      final val = CycleRunFailedEvent(
        runId: $checkedConvert('runId', (v) => v as String),
        cycleId: $checkedConvert('cycleId', (v) => v as String?),
        situationId: $checkedConvert('situationId', (v) => v as String?),
        cadence: $checkedConvert(
          'cadence',
          (v) => $enumDecode(_$CycleRunFailedEventCadenceEnumEnumMap, v),
        ),
        reason: $checkedConvert('reason', (v) => v as String),
        at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
      );
      return val;
    });

Map<String, dynamic> _$CycleRunFailedEventToJson(
  CycleRunFailedEvent instance,
) => <String, dynamic>{
  'runId': instance.runId,
  'cycleId': ?instance.cycleId,
  'situationId': ?instance.situationId,
  'cadence': _$CycleRunFailedEventCadenceEnumEnumMap[instance.cadence]!,
  'reason': instance.reason,
  'at': instance.at.toIso8601String(),
};

const _$CycleRunFailedEventCadenceEnumEnumMap = {
  CycleRunFailedEventCadenceEnum.daily: 'daily',
  CycleRunFailedEventCadenceEnum.weekly: 'weekly',
  CycleRunFailedEventCadenceEnum.monthly: 'monthly',
  CycleRunFailedEventCadenceEnum.onDemand: 'on-demand',
};
