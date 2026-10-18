// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'notification_read_request_one_of1.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NotificationReadRequestOneOf1CWProxy {
  NotificationReadRequestOneOf1 ids(List<int> ids);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NotificationReadRequestOneOf1(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NotificationReadRequestOneOf1(...).copyWith(id: 12, name: "My name")
  /// ```
  NotificationReadRequestOneOf1 call({List<int> ids});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNotificationReadRequestOneOf1.copyWith(...)` or call `instanceOfNotificationReadRequestOneOf1.copyWith.fieldName(value)` for a single field.
class _$NotificationReadRequestOneOf1CWProxyImpl
    implements _$NotificationReadRequestOneOf1CWProxy {
  const _$NotificationReadRequestOneOf1CWProxyImpl(this._value);

  final NotificationReadRequestOneOf1 _value;

  @override
  NotificationReadRequestOneOf1 ids(List<int> ids) => call(ids: ids);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NotificationReadRequestOneOf1(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NotificationReadRequestOneOf1(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NotificationReadRequestOneOf1 call({
    Object? ids = const $CopyWithPlaceholder(),
  }) {
    return NotificationReadRequestOneOf1(
      ids: ids == const $CopyWithPlaceholder() || ids == null
          ? _value.ids
          // ignore: cast_nullable_to_non_nullable
          : ids as List<int>,
    );
  }
}

extension $NotificationReadRequestOneOf1CopyWith
    on NotificationReadRequestOneOf1 {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNotificationReadRequestOneOf1.copyWith(...)` or `instanceOfNotificationReadRequestOneOf1.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NotificationReadRequestOneOf1CWProxy get copyWith =>
      _$NotificationReadRequestOneOf1CWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NotificationReadRequestOneOf1 _$NotificationReadRequestOneOf1FromJson(
  Map<String, dynamic> json,
) => $checkedCreate('NotificationReadRequestOneOf1', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['ids']);
  final val = NotificationReadRequestOneOf1(
    ids: $checkedConvert(
      'ids',
      (v) => (v as List<dynamic>).map((e) => (e as num).toInt()).toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$NotificationReadRequestOneOf1ToJson(
  NotificationReadRequestOneOf1 instance,
) => <String, dynamic>{'ids': instance.ids};
