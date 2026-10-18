// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'taxii_discovery.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TaxiiDiscoveryCWProxy {
  TaxiiDiscovery title(String title);

  TaxiiDiscovery description(String? description);

  TaxiiDiscovery contact(String? contact);

  TaxiiDiscovery default_(String? default_);

  TaxiiDiscovery apiRoots(List<String>? apiRoots);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TaxiiDiscovery(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TaxiiDiscovery(...).copyWith(id: 12, name: "My name")
  /// ```
  TaxiiDiscovery call({
    String title,
    String? description,
    String? contact,
    String? default_,
    List<String>? apiRoots,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTaxiiDiscovery.copyWith(...)` or call `instanceOfTaxiiDiscovery.copyWith.fieldName(value)` for a single field.
class _$TaxiiDiscoveryCWProxyImpl implements _$TaxiiDiscoveryCWProxy {
  const _$TaxiiDiscoveryCWProxyImpl(this._value);

  final TaxiiDiscovery _value;

  @override
  TaxiiDiscovery title(String title) => call(title: title);

  @override
  TaxiiDiscovery description(String? description) =>
      call(description: description);

  @override
  TaxiiDiscovery contact(String? contact) => call(contact: contact);

  @override
  TaxiiDiscovery default_(String? default_) => call(default_: default_);

  @override
  TaxiiDiscovery apiRoots(List<String>? apiRoots) => call(apiRoots: apiRoots);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TaxiiDiscovery(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TaxiiDiscovery(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TaxiiDiscovery call({
    Object? title = const $CopyWithPlaceholder(),
    Object? description = const $CopyWithPlaceholder(),
    Object? contact = const $CopyWithPlaceholder(),
    Object? default_ = const $CopyWithPlaceholder(),
    Object? apiRoots = const $CopyWithPlaceholder(),
  }) {
    return TaxiiDiscovery(
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
      description: description == const $CopyWithPlaceholder()
          ? _value.description
          // ignore: cast_nullable_to_non_nullable
          : description as String?,
      contact: contact == const $CopyWithPlaceholder()
          ? _value.contact
          // ignore: cast_nullable_to_non_nullable
          : contact as String?,
      default_: default_ == const $CopyWithPlaceholder()
          ? _value.default_
          // ignore: cast_nullable_to_non_nullable
          : default_ as String?,
      apiRoots: apiRoots == const $CopyWithPlaceholder()
          ? _value.apiRoots
          // ignore: cast_nullable_to_non_nullable
          : apiRoots as List<String>?,
    );
  }
}

extension $TaxiiDiscoveryCopyWith on TaxiiDiscovery {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTaxiiDiscovery.copyWith(...)` or `instanceOfTaxiiDiscovery.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TaxiiDiscoveryCWProxy get copyWith => _$TaxiiDiscoveryCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TaxiiDiscovery _$TaxiiDiscoveryFromJson(Map<String, dynamic> json) =>
    $checkedCreate('TaxiiDiscovery', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['title']);
      final val = TaxiiDiscovery(
        title: $checkedConvert('title', (v) => v as String),
        description: $checkedConvert('description', (v) => v as String?),
        contact: $checkedConvert('contact', (v) => v as String?),
        default_: $checkedConvert('default', (v) => v as String?),
        apiRoots: $checkedConvert(
          'api_roots',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
      );
      return val;
    }, fieldKeyMap: const {'default_': 'default', 'apiRoots': 'api_roots'});

Map<String, dynamic> _$TaxiiDiscoveryToJson(TaxiiDiscovery instance) =>
    <String, dynamic>{
      'title': instance.title,
      'description': ?instance.description,
      'contact': ?instance.contact,
      'default': ?instance.default_,
      'api_roots': ?instance.apiRoots,
    };
