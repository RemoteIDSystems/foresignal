// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'edp_search.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EdpSearchCWProxy {
  EdpSearch url(String url);

  EdpSearch auth(EdpSearchAuthEnum? auth);

  EdpSearch header(String? header);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpSearch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpSearch(...).copyWith(id: 12, name: "My name")
  /// ```
  EdpSearch call({String url, EdpSearchAuthEnum? auth, String? header});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEdpSearch.copyWith(...)` or call `instanceOfEdpSearch.copyWith.fieldName(value)` for a single field.
class _$EdpSearchCWProxyImpl implements _$EdpSearchCWProxy {
  const _$EdpSearchCWProxyImpl(this._value);

  final EdpSearch _value;

  @override
  EdpSearch url(String url) => call(url: url);

  @override
  EdpSearch auth(EdpSearchAuthEnum? auth) => call(auth: auth);

  @override
  EdpSearch header(String? header) => call(header: header);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpSearch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpSearch(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EdpSearch call({
    Object? url = const $CopyWithPlaceholder(),
    Object? auth = const $CopyWithPlaceholder(),
    Object? header = const $CopyWithPlaceholder(),
  }) {
    return EdpSearch(
      url: url == const $CopyWithPlaceholder() || url == null
          ? _value.url
          // ignore: cast_nullable_to_non_nullable
          : url as String,
      auth: auth == const $CopyWithPlaceholder()
          ? _value.auth
          // ignore: cast_nullable_to_non_nullable
          : auth as EdpSearchAuthEnum?,
      header: header == const $CopyWithPlaceholder()
          ? _value.header
          // ignore: cast_nullable_to_non_nullable
          : header as String?,
    );
  }
}

extension $EdpSearchCopyWith on EdpSearch {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEdpSearch.copyWith(...)` or `instanceOfEdpSearch.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EdpSearchCWProxy get copyWith => _$EdpSearchCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EdpSearch _$EdpSearchFromJson(Map<String, dynamic> json) =>
    $checkedCreate('EdpSearch', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['url']);
      final val = EdpSearch(
        url: $checkedConvert('url', (v) => v as String),
        auth: $checkedConvert(
          'auth',
          (v) => $enumDecodeNullable(_$EdpSearchAuthEnumEnumMap, v),
        ),
        header: $checkedConvert('header', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$EdpSearchToJson(EdpSearch instance) => <String, dynamic>{
  'url': instance.url,
  'auth': ?_$EdpSearchAuthEnumEnumMap[instance.auth],
  'header': ?instance.header,
};

const _$EdpSearchAuthEnumEnumMap = {
  EdpSearchAuthEnum.none: 'none',
  EdpSearchAuthEnum.bearer: 'bearer',
  EdpSearchAuthEnum.header: 'header',
  EdpSearchAuthEnum.basic: 'basic',
};
