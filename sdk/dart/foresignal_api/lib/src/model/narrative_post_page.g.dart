// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'narrative_post_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NarrativePostPageCWProxy {
  NarrativePostPage items(List<NarrativePost> items);

  NarrativePostPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NarrativePostPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NarrativePostPage(...).copyWith(id: 12, name: "My name")
  /// ```
  NarrativePostPage call({List<NarrativePost> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNarrativePostPage.copyWith(...)` or call `instanceOfNarrativePostPage.copyWith.fieldName(value)` for a single field.
class _$NarrativePostPageCWProxyImpl implements _$NarrativePostPageCWProxy {
  const _$NarrativePostPageCWProxyImpl(this._value);

  final NarrativePostPage _value;

  @override
  NarrativePostPage items(List<NarrativePost> items) => call(items: items);

  @override
  NarrativePostPage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NarrativePostPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NarrativePostPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NarrativePostPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return NarrativePostPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<NarrativePost>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $NarrativePostPageCopyWith on NarrativePostPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNarrativePostPage.copyWith(...)` or `instanceOfNarrativePostPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NarrativePostPageCWProxy get copyWith =>
      _$NarrativePostPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NarrativePostPage _$NarrativePostPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('NarrativePostPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items', 'nextCursor']);
      final val = NarrativePostPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => NarrativePost.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$NarrativePostPageToJson(NarrativePostPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': instance.nextCursor,
    };
