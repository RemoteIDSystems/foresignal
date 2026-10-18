// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'taxii_api_root.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TaxiiApiRootCWProxy {
  TaxiiApiRoot title(String title);

  TaxiiApiRoot description(String? description);

  TaxiiApiRoot versions(List<String> versions);

  TaxiiApiRoot maxContentLength(int maxContentLength);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TaxiiApiRoot(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TaxiiApiRoot(...).copyWith(id: 12, name: "My name")
  /// ```
  TaxiiApiRoot call({
    String title,
    String? description,
    List<String> versions,
    int maxContentLength,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTaxiiApiRoot.copyWith(...)` or call `instanceOfTaxiiApiRoot.copyWith.fieldName(value)` for a single field.
class _$TaxiiApiRootCWProxyImpl implements _$TaxiiApiRootCWProxy {
  const _$TaxiiApiRootCWProxyImpl(this._value);

  final TaxiiApiRoot _value;

  @override
  TaxiiApiRoot title(String title) => call(title: title);

  @override
  TaxiiApiRoot description(String? description) =>
      call(description: description);

  @override
  TaxiiApiRoot versions(List<String> versions) => call(versions: versions);

  @override
  TaxiiApiRoot maxContentLength(int maxContentLength) =>
      call(maxContentLength: maxContentLength);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TaxiiApiRoot(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TaxiiApiRoot(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TaxiiApiRoot call({
    Object? title = const $CopyWithPlaceholder(),
    Object? description = const $CopyWithPlaceholder(),
    Object? versions = const $CopyWithPlaceholder(),
    Object? maxContentLength = const $CopyWithPlaceholder(),
  }) {
    return TaxiiApiRoot(
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
      description: description == const $CopyWithPlaceholder()
          ? _value.description
          // ignore: cast_nullable_to_non_nullable
          : description as String?,
      versions: versions == const $CopyWithPlaceholder() || versions == null
          ? _value.versions
          // ignore: cast_nullable_to_non_nullable
          : versions as List<String>,
      maxContentLength:
          maxContentLength == const $CopyWithPlaceholder() ||
              maxContentLength == null
          ? _value.maxContentLength
          // ignore: cast_nullable_to_non_nullable
          : maxContentLength as int,
    );
  }
}

extension $TaxiiApiRootCopyWith on TaxiiApiRoot {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTaxiiApiRoot.copyWith(...)` or `instanceOfTaxiiApiRoot.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TaxiiApiRootCWProxy get copyWith => _$TaxiiApiRootCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TaxiiApiRoot _$TaxiiApiRootFromJson(Map<String, dynamic> json) =>
    $checkedCreate('TaxiiApiRoot', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['title', 'versions', 'max_content_length'],
      );
      final val = TaxiiApiRoot(
        title: $checkedConvert('title', (v) => v as String),
        description: $checkedConvert('description', (v) => v as String?),
        versions: $checkedConvert(
          'versions',
          (v) => (v as List<dynamic>).map((e) => e as String).toList(),
        ),
        maxContentLength: $checkedConvert(
          'max_content_length',
          (v) => (v as num).toInt(),
        ),
      );
      return val;
    }, fieldKeyMap: const {'maxContentLength': 'max_content_length'});

Map<String, dynamic> _$TaxiiApiRootToJson(TaxiiApiRoot instance) =>
    <String, dynamic>{
      'title': instance.title,
      'description': ?instance.description,
      'versions': instance.versions,
      'max_content_length': instance.maxContentLength,
    };
