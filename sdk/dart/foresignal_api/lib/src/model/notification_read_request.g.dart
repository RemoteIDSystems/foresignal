// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'notification_read_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NotificationReadRequestCWProxy {
  NotificationReadRequest upToId(int upToId);

  NotificationReadRequest ids(List<int> ids);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NotificationReadRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NotificationReadRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  NotificationReadRequest call({int upToId, List<int> ids});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNotificationReadRequest.copyWith(...)` or call `instanceOfNotificationReadRequest.copyWith.fieldName(value)` for a single field.
class _$NotificationReadRequestCWProxyImpl
    implements _$NotificationReadRequestCWProxy {
  const _$NotificationReadRequestCWProxyImpl(this._value);

  final NotificationReadRequest _value;

  @override
  NotificationReadRequest upToId(int upToId) => call(upToId: upToId);

  @override
  NotificationReadRequest ids(List<int> ids) => call(ids: ids);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NotificationReadRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NotificationReadRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NotificationReadRequest call({
    Object? upToId = const $CopyWithPlaceholder(),
    Object? ids = const $CopyWithPlaceholder(),
  }) {
    return NotificationReadRequest(
      upToId: upToId == const $CopyWithPlaceholder() || upToId == null
          ? _value.upToId
          // ignore: cast_nullable_to_non_nullable
          : upToId as int,
      ids: ids == const $CopyWithPlaceholder() || ids == null
          ? _value.ids
          // ignore: cast_nullable_to_non_nullable
          : ids as List<int>,
    );
  }
}

extension $NotificationReadRequestCopyWith on NotificationReadRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNotificationReadRequest.copyWith(...)` or `instanceOfNotificationReadRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NotificationReadRequestCWProxy get copyWith =>
      _$NotificationReadRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NotificationReadRequest _$NotificationReadRequestFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('NotificationReadRequest', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['upToId', 'ids']);
  final val = NotificationReadRequest(
    upToId: $checkedConvert('upToId', (v) => (v as num).toInt()),
    ids: $checkedConvert(
      'ids',
      (v) => (v as List<dynamic>).map((e) => (e as num).toInt()).toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$NotificationReadRequestToJson(
  NotificationReadRequest instance,
) => <String, dynamic>{'upToId': instance.upToId, 'ids': instance.ids};
