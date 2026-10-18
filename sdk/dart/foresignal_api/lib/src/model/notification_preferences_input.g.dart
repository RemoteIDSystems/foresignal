// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'notification_preferences_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NotificationPreferencesInputCWProxy {
  NotificationPreferencesInput items(List<NotificationPreferenceInput> items);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NotificationPreferencesInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NotificationPreferencesInput(...).copyWith(id: 12, name: "My name")
  /// ```
  NotificationPreferencesInput call({List<NotificationPreferenceInput> items});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNotificationPreferencesInput.copyWith(...)` or call `instanceOfNotificationPreferencesInput.copyWith.fieldName(value)` for a single field.
class _$NotificationPreferencesInputCWProxyImpl
    implements _$NotificationPreferencesInputCWProxy {
  const _$NotificationPreferencesInputCWProxyImpl(this._value);

  final NotificationPreferencesInput _value;

  @override
  NotificationPreferencesInput items(List<NotificationPreferenceInput> items) =>
      call(items: items);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NotificationPreferencesInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NotificationPreferencesInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NotificationPreferencesInput call({
    Object? items = const $CopyWithPlaceholder(),
  }) {
    return NotificationPreferencesInput(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<NotificationPreferenceInput>,
    );
  }
}

extension $NotificationPreferencesInputCopyWith
    on NotificationPreferencesInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNotificationPreferencesInput.copyWith(...)` or `instanceOfNotificationPreferencesInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NotificationPreferencesInputCWProxy get copyWith =>
      _$NotificationPreferencesInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NotificationPreferencesInput _$NotificationPreferencesInputFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('NotificationPreferencesInput', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['items']);
  final val = NotificationPreferencesInput(
    items: $checkedConvert(
      'items',
      (v) => (v as List<dynamic>)
          .map(
            (e) =>
                NotificationPreferenceInput.fromJson(e as Map<String, dynamic>),
          )
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$NotificationPreferencesInputToJson(
  NotificationPreferencesInput instance,
) => <String, dynamic>{'items': instance.items.map((e) => e.toJson()).toList()};
