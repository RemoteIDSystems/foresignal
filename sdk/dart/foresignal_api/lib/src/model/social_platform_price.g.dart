// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_platform_price.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialPlatformPriceCWProxy {
  SocialPlatformPrice item(String item);

  SocialPlatformPrice usd(num usd);

  SocialPlatformPrice per(int? per);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialPlatformPrice(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialPlatformPrice(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialPlatformPrice call({String item, num usd, int? per});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialPlatformPrice.copyWith(...)` or call `instanceOfSocialPlatformPrice.copyWith.fieldName(value)` for a single field.
class _$SocialPlatformPriceCWProxyImpl implements _$SocialPlatformPriceCWProxy {
  const _$SocialPlatformPriceCWProxyImpl(this._value);

  final SocialPlatformPrice _value;

  @override
  SocialPlatformPrice item(String item) => call(item: item);

  @override
  SocialPlatformPrice usd(num usd) => call(usd: usd);

  @override
  SocialPlatformPrice per(int? per) => call(per: per);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialPlatformPrice(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialPlatformPrice(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialPlatformPrice call({
    Object? item = const $CopyWithPlaceholder(),
    Object? usd = const $CopyWithPlaceholder(),
    Object? per = const $CopyWithPlaceholder(),
  }) {
    return SocialPlatformPrice(
      item: item == const $CopyWithPlaceholder() || item == null
          ? _value.item
          // ignore: cast_nullable_to_non_nullable
          : item as String,
      usd: usd == const $CopyWithPlaceholder() || usd == null
          ? _value.usd
          // ignore: cast_nullable_to_non_nullable
          : usd as num,
      per: per == const $CopyWithPlaceholder()
          ? _value.per
          // ignore: cast_nullable_to_non_nullable
          : per as int?,
    );
  }
}

extension $SocialPlatformPriceCopyWith on SocialPlatformPrice {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialPlatformPrice.copyWith(...)` or `instanceOfSocialPlatformPrice.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialPlatformPriceCWProxy get copyWith =>
      _$SocialPlatformPriceCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialPlatformPrice _$SocialPlatformPriceFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SocialPlatformPrice', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['item', 'usd']);
      final val = SocialPlatformPrice(
        item: $checkedConvert('item', (v) => v as String),
        usd: $checkedConvert('usd', (v) => v as num),
        per: $checkedConvert('per', (v) => (v as num?)?.toInt() ?? 1),
      );
      return val;
    });

Map<String, dynamic> _$SocialPlatformPriceToJson(
  SocialPlatformPrice instance,
) => <String, dynamic>{
  'item': instance.item,
  'usd': instance.usd,
  'per': ?instance.per,
};
