// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_platform_stream_last_gap.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialPlatformStreamLastGapCWProxy {
  SocialPlatformStreamLastGap from(DateTime from);

  SocialPlatformStreamLastGap to(DateTime to);

  SocialPlatformStreamLastGap replayed(bool replayed);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialPlatformStreamLastGap(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialPlatformStreamLastGap(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialPlatformStreamLastGap call({DateTime from, DateTime to, bool replayed});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialPlatformStreamLastGap.copyWith(...)` or call `instanceOfSocialPlatformStreamLastGap.copyWith.fieldName(value)` for a single field.
class _$SocialPlatformStreamLastGapCWProxyImpl
    implements _$SocialPlatformStreamLastGapCWProxy {
  const _$SocialPlatformStreamLastGapCWProxyImpl(this._value);

  final SocialPlatformStreamLastGap _value;

  @override
  SocialPlatformStreamLastGap from(DateTime from) => call(from: from);

  @override
  SocialPlatformStreamLastGap to(DateTime to) => call(to: to);

  @override
  SocialPlatformStreamLastGap replayed(bool replayed) =>
      call(replayed: replayed);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialPlatformStreamLastGap(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialPlatformStreamLastGap(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialPlatformStreamLastGap call({
    Object? from = const $CopyWithPlaceholder(),
    Object? to = const $CopyWithPlaceholder(),
    Object? replayed = const $CopyWithPlaceholder(),
  }) {
    return SocialPlatformStreamLastGap(
      from: from == const $CopyWithPlaceholder() || from == null
          ? _value.from
          // ignore: cast_nullable_to_non_nullable
          : from as DateTime,
      to: to == const $CopyWithPlaceholder() || to == null
          ? _value.to
          // ignore: cast_nullable_to_non_nullable
          : to as DateTime,
      replayed: replayed == const $CopyWithPlaceholder() || replayed == null
          ? _value.replayed
          // ignore: cast_nullable_to_non_nullable
          : replayed as bool,
    );
  }
}

extension $SocialPlatformStreamLastGapCopyWith on SocialPlatformStreamLastGap {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialPlatformStreamLastGap.copyWith(...)` or `instanceOfSocialPlatformStreamLastGap.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialPlatformStreamLastGapCWProxy get copyWith =>
      _$SocialPlatformStreamLastGapCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialPlatformStreamLastGap _$SocialPlatformStreamLastGapFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SocialPlatformStreamLastGap', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['from', 'to', 'replayed']);
  final val = SocialPlatformStreamLastGap(
    from: $checkedConvert('from', (v) => DateTime.parse(v as String)),
    to: $checkedConvert('to', (v) => DateTime.parse(v as String)),
    replayed: $checkedConvert('replayed', (v) => v as bool),
  );
  return val;
});

Map<String, dynamic> _$SocialPlatformStreamLastGapToJson(
  SocialPlatformStreamLastGap instance,
) => <String, dynamic>{
  'from': instance.from.toIso8601String(),
  'to': instance.to.toIso8601String(),
  'replayed': instance.replayed,
};
