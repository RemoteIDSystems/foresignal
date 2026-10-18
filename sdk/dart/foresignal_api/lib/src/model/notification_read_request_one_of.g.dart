// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'notification_read_request_one_of.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NotificationReadRequestOneOfCWProxy {
  NotificationReadRequestOneOf upToId(int upToId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NotificationReadRequestOneOf(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NotificationReadRequestOneOf(...).copyWith(id: 12, name: "My name")
  /// ```
  NotificationReadRequestOneOf call({int upToId});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNotificationReadRequestOneOf.copyWith(...)` or call `instanceOfNotificationReadRequestOneOf.copyWith.fieldName(value)` for a single field.
class _$NotificationReadRequestOneOfCWProxyImpl
    implements _$NotificationReadRequestOneOfCWProxy {
  const _$NotificationReadRequestOneOfCWProxyImpl(this._value);

  final NotificationReadRequestOneOf _value;

  @override
  NotificationReadRequestOneOf upToId(int upToId) => call(upToId: upToId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NotificationReadRequestOneOf(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NotificationReadRequestOneOf(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NotificationReadRequestOneOf call({
    Object? upToId = const $CopyWithPlaceholder(),
  }) {
    return NotificationReadRequestOneOf(
      upToId: upToId == const $CopyWithPlaceholder() || upToId == null
          ? _value.upToId
          // ignore: cast_nullable_to_non_nullable
          : upToId as int,
    );
  }
}

extension $NotificationReadRequestOneOfCopyWith
    on NotificationReadRequestOneOf {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNotificationReadRequestOneOf.copyWith(...)` or `instanceOfNotificationReadRequestOneOf.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NotificationReadRequestOneOfCWProxy get copyWith =>
      _$NotificationReadRequestOneOfCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NotificationReadRequestOneOf _$NotificationReadRequestOneOfFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('NotificationReadRequestOneOf', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['upToId']);
  final val = NotificationReadRequestOneOf(
    upToId: $checkedConvert('upToId', (v) => (v as num).toInt()),
  );
  return val;
});

Map<String, dynamic> _$NotificationReadRequestOneOfToJson(
  NotificationReadRequestOneOf instance,
) => <String, dynamic>{'upToId': instance.upToId};
