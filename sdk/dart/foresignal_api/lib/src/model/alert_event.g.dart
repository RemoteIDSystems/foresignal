// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'alert_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AlertEventCWProxy {
  AlertEvent type(AlertEventTypeEnum type);

  AlertEvent alert(Alert alert);

  AlertEvent suppressed(bool? suppressed);

  AlertEvent exercise(bool? exercise);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AlertEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AlertEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  AlertEvent call({
    AlertEventTypeEnum type,
    Alert alert,
    bool? suppressed,
    bool? exercise,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAlertEvent.copyWith(...)` or call `instanceOfAlertEvent.copyWith.fieldName(value)` for a single field.
class _$AlertEventCWProxyImpl implements _$AlertEventCWProxy {
  const _$AlertEventCWProxyImpl(this._value);

  final AlertEvent _value;

  @override
  AlertEvent type(AlertEventTypeEnum type) => call(type: type);

  @override
  AlertEvent alert(Alert alert) => call(alert: alert);

  @override
  AlertEvent suppressed(bool? suppressed) => call(suppressed: suppressed);

  @override
  AlertEvent exercise(bool? exercise) => call(exercise: exercise);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AlertEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AlertEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AlertEvent call({
    Object? type = const $CopyWithPlaceholder(),
    Object? alert = const $CopyWithPlaceholder(),
    Object? suppressed = const $CopyWithPlaceholder(),
    Object? exercise = const $CopyWithPlaceholder(),
  }) {
    return AlertEvent(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as AlertEventTypeEnum,
      alert: alert == const $CopyWithPlaceholder() || alert == null
          ? _value.alert
          // ignore: cast_nullable_to_non_nullable
          : alert as Alert,
      suppressed: suppressed == const $CopyWithPlaceholder()
          ? _value.suppressed
          // ignore: cast_nullable_to_non_nullable
          : suppressed as bool?,
      exercise: exercise == const $CopyWithPlaceholder()
          ? _value.exercise
          // ignore: cast_nullable_to_non_nullable
          : exercise as bool?,
    );
  }
}

extension $AlertEventCopyWith on AlertEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAlertEvent.copyWith(...)` or `instanceOfAlertEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AlertEventCWProxy get copyWith => _$AlertEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AlertEvent _$AlertEventFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AlertEvent', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['type', 'alert']);
      final val = AlertEvent(
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(_$AlertEventTypeEnumEnumMap, v),
        ),
        alert: $checkedConvert(
          'alert',
          (v) => Alert.fromJson(v as Map<String, dynamic>),
        ),
        suppressed: $checkedConvert('suppressed', (v) => v as bool? ?? false),
        exercise: $checkedConvert('exercise', (v) => v as bool? ?? false),
      );
      return val;
    });

Map<String, dynamic> _$AlertEventToJson(AlertEvent instance) =>
    <String, dynamic>{
      'type': _$AlertEventTypeEnumEnumMap[instance.type]!,
      'alert': instance.alert.toJson(),
      'suppressed': ?instance.suppressed,
      'exercise': ?instance.exercise,
    };

const _$AlertEventTypeEnumEnumMap = {
  AlertEventTypeEnum.fired: 'fired',
  AlertEventTypeEnum.updated: 'updated',
  AlertEventTypeEnum.acknowledged: 'acknowledged',
  AlertEventTypeEnum.snoozed: 'snoozed',
  AlertEventTypeEnum.resolved: 'resolved',
};
