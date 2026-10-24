// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'edp_analysis_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EdpAnalysisRequestCWProxy {
  EdpAnalysisRequest url(String url);

  EdpAnalysisRequest auth(EdpAnalysisRequestAuthEnum? auth);

  EdpAnalysisRequest header(String? header);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpAnalysisRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpAnalysisRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  EdpAnalysisRequest call({
    String url,
    EdpAnalysisRequestAuthEnum? auth,
    String? header,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEdpAnalysisRequest.copyWith(...)` or call `instanceOfEdpAnalysisRequest.copyWith.fieldName(value)` for a single field.
class _$EdpAnalysisRequestCWProxyImpl implements _$EdpAnalysisRequestCWProxy {
  const _$EdpAnalysisRequestCWProxyImpl(this._value);

  final EdpAnalysisRequest _value;

  @override
  EdpAnalysisRequest url(String url) => call(url: url);

  @override
  EdpAnalysisRequest auth(EdpAnalysisRequestAuthEnum? auth) => call(auth: auth);

  @override
  EdpAnalysisRequest header(String? header) => call(header: header);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpAnalysisRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpAnalysisRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EdpAnalysisRequest call({
    Object? url = const $CopyWithPlaceholder(),
    Object? auth = const $CopyWithPlaceholder(),
    Object? header = const $CopyWithPlaceholder(),
  }) {
    return EdpAnalysisRequest(
      url: url == const $CopyWithPlaceholder() || url == null
          ? _value.url
          // ignore: cast_nullable_to_non_nullable
          : url as String,
      auth: auth == const $CopyWithPlaceholder()
          ? _value.auth
          // ignore: cast_nullable_to_non_nullable
          : auth as EdpAnalysisRequestAuthEnum?,
      header: header == const $CopyWithPlaceholder()
          ? _value.header
          // ignore: cast_nullable_to_non_nullable
          : header as String?,
    );
  }
}

extension $EdpAnalysisRequestCopyWith on EdpAnalysisRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEdpAnalysisRequest.copyWith(...)` or `instanceOfEdpAnalysisRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EdpAnalysisRequestCWProxy get copyWith =>
      _$EdpAnalysisRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EdpAnalysisRequest _$EdpAnalysisRequestFromJson(Map<String, dynamic> json) =>
    $checkedCreate('EdpAnalysisRequest', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['url']);
      final val = EdpAnalysisRequest(
        url: $checkedConvert('url', (v) => v as String),
        auth: $checkedConvert(
          'auth',
          (v) => $enumDecodeNullable(_$EdpAnalysisRequestAuthEnumEnumMap, v),
        ),
        header: $checkedConvert('header', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$EdpAnalysisRequestToJson(EdpAnalysisRequest instance) =>
    <String, dynamic>{
      'url': instance.url,
      'auth': ?_$EdpAnalysisRequestAuthEnumEnumMap[instance.auth],
      'header': ?instance.header,
    };

const _$EdpAnalysisRequestAuthEnumEnumMap = {
  EdpAnalysisRequestAuthEnum.none: 'none',
  EdpAnalysisRequestAuthEnum.bearer: 'bearer',
  EdpAnalysisRequestAuthEnum.header: 'header',
  EdpAnalysisRequestAuthEnum.basic: 'basic',
};
