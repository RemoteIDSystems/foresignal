// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'mapping_sample.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MappingSampleCWProxy {
  MappingSample contentType(String contentType);

  MappingSample body(Object? body);

  MappingSample query(Map<String, String>? query);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MappingSample(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MappingSample(...).copyWith(id: 12, name: "My name")
  /// ```
  MappingSample call({
    String contentType,
    Object? body,
    Map<String, String>? query,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMappingSample.copyWith(...)` or call `instanceOfMappingSample.copyWith.fieldName(value)` for a single field.
class _$MappingSampleCWProxyImpl implements _$MappingSampleCWProxy {
  const _$MappingSampleCWProxyImpl(this._value);

  final MappingSample _value;

  @override
  MappingSample contentType(String contentType) =>
      call(contentType: contentType);

  @override
  MappingSample body(Object? body) => call(body: body);

  @override
  MappingSample query(Map<String, String>? query) => call(query: query);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MappingSample(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MappingSample(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MappingSample call({
    Object? contentType = const $CopyWithPlaceholder(),
    Object? body = const $CopyWithPlaceholder(),
    Object? query = const $CopyWithPlaceholder(),
  }) {
    return MappingSample(
      contentType:
          contentType == const $CopyWithPlaceholder() || contentType == null
          ? _value.contentType
          // ignore: cast_nullable_to_non_nullable
          : contentType as String,
      body: body == const $CopyWithPlaceholder()
          ? _value.body
          // ignore: cast_nullable_to_non_nullable
          : body as Object?,
      query: query == const $CopyWithPlaceholder()
          ? _value.query
          // ignore: cast_nullable_to_non_nullable
          : query as Map<String, String>?,
    );
  }
}

extension $MappingSampleCopyWith on MappingSample {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMappingSample.copyWith(...)` or `instanceOfMappingSample.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MappingSampleCWProxy get copyWith => _$MappingSampleCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MappingSample _$MappingSampleFromJson(Map<String, dynamic> json) =>
    $checkedCreate('MappingSample', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['contentType']);
      final val = MappingSample(
        contentType: $checkedConvert('contentType', (v) => v as String),
        body: $checkedConvert('body', (v) => v),
        query: $checkedConvert(
          'query',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, e as String),
          ),
        ),
      );
      return val;
    });

Map<String, dynamic> _$MappingSampleToJson(MappingSample instance) =>
    <String, dynamic>{
      'contentType': instance.contentType,
      'body': ?instance.body,
      'query': ?instance.query,
    };
