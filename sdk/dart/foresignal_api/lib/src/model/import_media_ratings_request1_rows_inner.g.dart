// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'import_media_ratings_request1_rows_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ImportMediaRatingsRequest1RowsInnerCWProxy {
  ImportMediaRatingsRequest1RowsInner domain(String domain);

  ImportMediaRatingsRequest1RowsInner dimension(Object? dimension);

  ImportMediaRatingsRequest1RowsInner value(String value);

  ImportMediaRatingsRequest1RowsInner label(String? label);

  ImportMediaRatingsRequest1RowsInner licence(String licence);

  ImportMediaRatingsRequest1RowsInner attribution(String attribution);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ImportMediaRatingsRequest1RowsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ImportMediaRatingsRequest1RowsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ImportMediaRatingsRequest1RowsInner call({
    String domain,
    Object? dimension,
    String value,
    String? label,
    String licence,
    String attribution,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfImportMediaRatingsRequest1RowsInner.copyWith(...)` or call `instanceOfImportMediaRatingsRequest1RowsInner.copyWith.fieldName(value)` for a single field.
class _$ImportMediaRatingsRequest1RowsInnerCWProxyImpl
    implements _$ImportMediaRatingsRequest1RowsInnerCWProxy {
  const _$ImportMediaRatingsRequest1RowsInnerCWProxyImpl(this._value);

  final ImportMediaRatingsRequest1RowsInner _value;

  @override
  ImportMediaRatingsRequest1RowsInner domain(String domain) =>
      call(domain: domain);

  @override
  ImportMediaRatingsRequest1RowsInner dimension(Object? dimension) =>
      call(dimension: dimension);

  @override
  ImportMediaRatingsRequest1RowsInner value(String value) => call(value: value);

  @override
  ImportMediaRatingsRequest1RowsInner label(String? label) =>
      call(label: label);

  @override
  ImportMediaRatingsRequest1RowsInner licence(String licence) =>
      call(licence: licence);

  @override
  ImportMediaRatingsRequest1RowsInner attribution(String attribution) =>
      call(attribution: attribution);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ImportMediaRatingsRequest1RowsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ImportMediaRatingsRequest1RowsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ImportMediaRatingsRequest1RowsInner call({
    Object? domain = const $CopyWithPlaceholder(),
    Object? dimension = const $CopyWithPlaceholder(),
    Object? value = const $CopyWithPlaceholder(),
    Object? label = const $CopyWithPlaceholder(),
    Object? licence = const $CopyWithPlaceholder(),
    Object? attribution = const $CopyWithPlaceholder(),
  }) {
    return ImportMediaRatingsRequest1RowsInner(
      domain: domain == const $CopyWithPlaceholder() || domain == null
          ? _value.domain
          // ignore: cast_nullable_to_non_nullable
          : domain as String,
      dimension: dimension == const $CopyWithPlaceholder()
          ? _value.dimension
          // ignore: cast_nullable_to_non_nullable
          : dimension as Object?,
      value: value == const $CopyWithPlaceholder() || value == null
          ? _value.value
          // ignore: cast_nullable_to_non_nullable
          : value as String,
      label: label == const $CopyWithPlaceholder()
          ? _value.label
          // ignore: cast_nullable_to_non_nullable
          : label as String?,
      licence: licence == const $CopyWithPlaceholder() || licence == null
          ? _value.licence
          // ignore: cast_nullable_to_non_nullable
          : licence as String,
      attribution:
          attribution == const $CopyWithPlaceholder() || attribution == null
          ? _value.attribution
          // ignore: cast_nullable_to_non_nullable
          : attribution as String,
    );
  }
}

extension $ImportMediaRatingsRequest1RowsInnerCopyWith
    on ImportMediaRatingsRequest1RowsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfImportMediaRatingsRequest1RowsInner.copyWith(...)` or `instanceOfImportMediaRatingsRequest1RowsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ImportMediaRatingsRequest1RowsInnerCWProxy get copyWith =>
      _$ImportMediaRatingsRequest1RowsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ImportMediaRatingsRequest1RowsInner
_$ImportMediaRatingsRequest1RowsInnerFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ImportMediaRatingsRequest1RowsInner', json, (
      $checkedConvert,
    ) {
      $checkKeys(
        json,
        requiredKeys: const [
          'domain',
          'dimension',
          'value',
          'licence',
          'attribution',
        ],
      );
      final val = ImportMediaRatingsRequest1RowsInner(
        domain: $checkedConvert('domain', (v) => v as String),
        dimension: $checkedConvert('dimension', (v) => v),
        value: $checkedConvert('value', (v) => v as String),
        label: $checkedConvert('label', (v) => v as String?),
        licence: $checkedConvert('licence', (v) => v as String),
        attribution: $checkedConvert('attribution', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$ImportMediaRatingsRequest1RowsInnerToJson(
  ImportMediaRatingsRequest1RowsInner instance,
) => <String, dynamic>{
  'domain': instance.domain,
  'dimension': instance.dimension,
  'value': instance.value,
  'label': ?instance.label,
  'licence': instance.licence,
  'attribution': instance.attribution,
};
