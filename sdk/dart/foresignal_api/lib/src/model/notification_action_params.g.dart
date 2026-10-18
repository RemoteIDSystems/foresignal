// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'notification_action_params.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NotificationActionParamsCWProxy {
  NotificationActionParams duration(String? duration);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NotificationActionParams(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NotificationActionParams(...).copyWith(id: 12, name: "My name")
  /// ```
  NotificationActionParams call({String? duration});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNotificationActionParams.copyWith(...)` or call `instanceOfNotificationActionParams.copyWith.fieldName(value)` for a single field.
class _$NotificationActionParamsCWProxyImpl
    implements _$NotificationActionParamsCWProxy {
  const _$NotificationActionParamsCWProxyImpl(this._value);

  final NotificationActionParams _value;

  @override
  NotificationActionParams duration(String? duration) =>
      call(duration: duration);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NotificationActionParams(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NotificationActionParams(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NotificationActionParams call({
    Object? duration = const $CopyWithPlaceholder(),
  }) {
    return NotificationActionParams(
      duration: duration == const $CopyWithPlaceholder()
          ? _value.duration
          // ignore: cast_nullable_to_non_nullable
          : duration as String?,
    );
  }
}

extension $NotificationActionParamsCopyWith on NotificationActionParams {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNotificationActionParams.copyWith(...)` or `instanceOfNotificationActionParams.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NotificationActionParamsCWProxy get copyWith =>
      _$NotificationActionParamsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NotificationActionParams _$NotificationActionParamsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('NotificationActionParams', json, ($checkedConvert) {
  final val = NotificationActionParams(
    duration: $checkedConvert('duration', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$NotificationActionParamsToJson(
  NotificationActionParams instance,
) => <String, dynamic>{'duration': ?instance.duration};
