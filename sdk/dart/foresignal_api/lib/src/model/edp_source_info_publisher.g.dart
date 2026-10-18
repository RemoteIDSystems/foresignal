// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'edp_source_info_publisher.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EdpSourceInfoPublisherCWProxy {
  EdpSourceInfoPublisher name(String name);

  EdpSourceInfoPublisher url(String? url);

  EdpSourceInfoPublisher contact(String? contact);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpSourceInfoPublisher(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpSourceInfoPublisher(...).copyWith(id: 12, name: "My name")
  /// ```
  EdpSourceInfoPublisher call({String name, String? url, String? contact});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEdpSourceInfoPublisher.copyWith(...)` or call `instanceOfEdpSourceInfoPublisher.copyWith.fieldName(value)` for a single field.
class _$EdpSourceInfoPublisherCWProxyImpl
    implements _$EdpSourceInfoPublisherCWProxy {
  const _$EdpSourceInfoPublisherCWProxyImpl(this._value);

  final EdpSourceInfoPublisher _value;

  @override
  EdpSourceInfoPublisher name(String name) => call(name: name);

  @override
  EdpSourceInfoPublisher url(String? url) => call(url: url);

  @override
  EdpSourceInfoPublisher contact(String? contact) => call(contact: contact);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpSourceInfoPublisher(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpSourceInfoPublisher(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EdpSourceInfoPublisher call({
    Object? name = const $CopyWithPlaceholder(),
    Object? url = const $CopyWithPlaceholder(),
    Object? contact = const $CopyWithPlaceholder(),
  }) {
    return EdpSourceInfoPublisher(
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      url: url == const $CopyWithPlaceholder()
          ? _value.url
          // ignore: cast_nullable_to_non_nullable
          : url as String?,
      contact: contact == const $CopyWithPlaceholder()
          ? _value.contact
          // ignore: cast_nullable_to_non_nullable
          : contact as String?,
    );
  }
}

extension $EdpSourceInfoPublisherCopyWith on EdpSourceInfoPublisher {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEdpSourceInfoPublisher.copyWith(...)` or `instanceOfEdpSourceInfoPublisher.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EdpSourceInfoPublisherCWProxy get copyWith =>
      _$EdpSourceInfoPublisherCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EdpSourceInfoPublisher _$EdpSourceInfoPublisherFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('EdpSourceInfoPublisher', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['name']);
  final val = EdpSourceInfoPublisher(
    name: $checkedConvert('name', (v) => v as String),
    url: $checkedConvert('url', (v) => v as String?),
    contact: $checkedConvert('contact', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$EdpSourceInfoPublisherToJson(
  EdpSourceInfoPublisher instance,
) => <String, dynamic>{
  'name': instance.name,
  'url': ?instance.url,
  'contact': ?instance.contact,
};
