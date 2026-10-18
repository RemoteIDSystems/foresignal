// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_cue_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialCuePageCWProxy {
  SocialCuePage items(List<SocialCue> items);

  SocialCuePage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialCuePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialCuePage(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialCuePage call({List<SocialCue> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialCuePage.copyWith(...)` or call `instanceOfSocialCuePage.copyWith.fieldName(value)` for a single field.
class _$SocialCuePageCWProxyImpl implements _$SocialCuePageCWProxy {
  const _$SocialCuePageCWProxyImpl(this._value);

  final SocialCuePage _value;

  @override
  SocialCuePage items(List<SocialCue> items) => call(items: items);

  @override
  SocialCuePage nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialCuePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialCuePage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialCuePage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return SocialCuePage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<SocialCue>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $SocialCuePageCopyWith on SocialCuePage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialCuePage.copyWith(...)` or `instanceOfSocialCuePage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialCuePageCWProxy get copyWith => _$SocialCuePageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialCuePage _$SocialCuePageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SocialCuePage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items', 'nextCursor']);
      final val = SocialCuePage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => SocialCue.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$SocialCuePageToJson(SocialCuePage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': instance.nextCursor,
    };
