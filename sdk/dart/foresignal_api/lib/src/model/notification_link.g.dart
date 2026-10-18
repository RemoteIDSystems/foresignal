// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'notification_link.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NotificationLinkCWProxy {
  NotificationLink label(String label);

  NotificationLink url(String url);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NotificationLink(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NotificationLink(...).copyWith(id: 12, name: "My name")
  /// ```
  NotificationLink call({String label, String url});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNotificationLink.copyWith(...)` or call `instanceOfNotificationLink.copyWith.fieldName(value)` for a single field.
class _$NotificationLinkCWProxyImpl implements _$NotificationLinkCWProxy {
  const _$NotificationLinkCWProxyImpl(this._value);

  final NotificationLink _value;

  @override
  NotificationLink label(String label) => call(label: label);

  @override
  NotificationLink url(String url) => call(url: url);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NotificationLink(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NotificationLink(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NotificationLink call({
    Object? label = const $CopyWithPlaceholder(),
    Object? url = const $CopyWithPlaceholder(),
  }) {
    return NotificationLink(
      label: label == const $CopyWithPlaceholder() || label == null
          ? _value.label
          // ignore: cast_nullable_to_non_nullable
          : label as String,
      url: url == const $CopyWithPlaceholder() || url == null
          ? _value.url
          // ignore: cast_nullable_to_non_nullable
          : url as String,
    );
  }
}

extension $NotificationLinkCopyWith on NotificationLink {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNotificationLink.copyWith(...)` or `instanceOfNotificationLink.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NotificationLinkCWProxy get copyWith => _$NotificationLinkCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NotificationLink _$NotificationLinkFromJson(Map<String, dynamic> json) =>
    $checkedCreate('NotificationLink', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['label', 'url']);
      final val = NotificationLink(
        label: $checkedConvert('label', (v) => v as String),
        url: $checkedConvert('url', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$NotificationLinkToJson(NotificationLink instance) =>
    <String, dynamic>{'label': instance.label, 'url': instance.url};
