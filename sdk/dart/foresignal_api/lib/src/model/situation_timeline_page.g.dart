// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'situation_timeline_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SituationTimelinePageCWProxy {
  SituationTimelinePage items(List<SituationTimelineEntry> items);

  SituationTimelinePage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationTimelinePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationTimelinePage(...).copyWith(id: 12, name: "My name")
  /// ```
  SituationTimelinePage call({
    List<SituationTimelineEntry> items,
    String? nextCursor,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSituationTimelinePage.copyWith(...)` or call `instanceOfSituationTimelinePage.copyWith.fieldName(value)` for a single field.
class _$SituationTimelinePageCWProxyImpl
    implements _$SituationTimelinePageCWProxy {
  const _$SituationTimelinePageCWProxyImpl(this._value);

  final SituationTimelinePage _value;

  @override
  SituationTimelinePage items(List<SituationTimelineEntry> items) =>
      call(items: items);

  @override
  SituationTimelinePage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationTimelinePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationTimelinePage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SituationTimelinePage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return SituationTimelinePage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<SituationTimelineEntry>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $SituationTimelinePageCopyWith on SituationTimelinePage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSituationTimelinePage.copyWith(...)` or `instanceOfSituationTimelinePage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SituationTimelinePageCWProxy get copyWith =>
      _$SituationTimelinePageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SituationTimelinePage _$SituationTimelinePageFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SituationTimelinePage', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['items']);
  final val = SituationTimelinePage(
    items: $checkedConvert(
      'items',
      (v) => (v as List<dynamic>)
          .map(
            (e) => SituationTimelineEntry.fromJson(e as Map<String, dynamic>),
          )
          .toList(),
    ),
    nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$SituationTimelinePageToJson(
  SituationTimelinePage instance,
) => <String, dynamic>{
  'items': instance.items.map((e) => e.toJson()).toList(),
  'nextCursor': ?instance.nextCursor,
};
