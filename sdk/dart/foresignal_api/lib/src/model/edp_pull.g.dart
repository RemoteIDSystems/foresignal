// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'edp_pull.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EdpPullCWProxy {
  EdpPull itemsUrl(String itemsUrl);

  EdpPull intervalSeconds(int? intervalSeconds);

  EdpPull auth(EdpPullAuthEnum? auth);

  EdpPull header(String? header);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpPull(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpPull(...).copyWith(id: 12, name: "My name")
  /// ```
  EdpPull call({
    String itemsUrl,
    int? intervalSeconds,
    EdpPullAuthEnum? auth,
    String? header,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEdpPull.copyWith(...)` or call `instanceOfEdpPull.copyWith.fieldName(value)` for a single field.
class _$EdpPullCWProxyImpl implements _$EdpPullCWProxy {
  const _$EdpPullCWProxyImpl(this._value);

  final EdpPull _value;

  @override
  EdpPull itemsUrl(String itemsUrl) => call(itemsUrl: itemsUrl);

  @override
  EdpPull intervalSeconds(int? intervalSeconds) =>
      call(intervalSeconds: intervalSeconds);

  @override
  EdpPull auth(EdpPullAuthEnum? auth) => call(auth: auth);

  @override
  EdpPull header(String? header) => call(header: header);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpPull(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpPull(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EdpPull call({
    Object? itemsUrl = const $CopyWithPlaceholder(),
    Object? intervalSeconds = const $CopyWithPlaceholder(),
    Object? auth = const $CopyWithPlaceholder(),
    Object? header = const $CopyWithPlaceholder(),
  }) {
    return EdpPull(
      itemsUrl: itemsUrl == const $CopyWithPlaceholder() || itemsUrl == null
          ? _value.itemsUrl
          // ignore: cast_nullable_to_non_nullable
          : itemsUrl as String,
      intervalSeconds: intervalSeconds == const $CopyWithPlaceholder()
          ? _value.intervalSeconds
          // ignore: cast_nullable_to_non_nullable
          : intervalSeconds as int?,
      auth: auth == const $CopyWithPlaceholder()
          ? _value.auth
          // ignore: cast_nullable_to_non_nullable
          : auth as EdpPullAuthEnum?,
      header: header == const $CopyWithPlaceholder()
          ? _value.header
          // ignore: cast_nullable_to_non_nullable
          : header as String?,
    );
  }
}

extension $EdpPullCopyWith on EdpPull {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEdpPull.copyWith(...)` or `instanceOfEdpPull.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EdpPullCWProxy get copyWith => _$EdpPullCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EdpPull _$EdpPullFromJson(Map<String, dynamic> json) =>
    $checkedCreate('EdpPull', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['itemsUrl']);
      final val = EdpPull(
        itemsUrl: $checkedConvert('itemsUrl', (v) => v as String),
        intervalSeconds: $checkedConvert(
          'intervalSeconds',
          (v) => (v as num?)?.toInt() ?? 300,
        ),
        auth: $checkedConvert(
          'auth',
          (v) => $enumDecodeNullable(_$EdpPullAuthEnumEnumMap, v),
        ),
        header: $checkedConvert('header', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$EdpPullToJson(EdpPull instance) => <String, dynamic>{
  'itemsUrl': instance.itemsUrl,
  'intervalSeconds': ?instance.intervalSeconds,
  'auth': ?_$EdpPullAuthEnumEnumMap[instance.auth],
  'header': ?instance.header,
};

const _$EdpPullAuthEnumEnumMap = {
  EdpPullAuthEnum.none: 'none',
  EdpPullAuthEnum.bearer: 'bearer',
  EdpPullAuthEnum.header: 'header',
  EdpPullAuthEnum.basic: 'basic',
};
