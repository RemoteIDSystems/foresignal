// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'hypothesis_revision_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$HypothesisRevisionPageCWProxy {
  HypothesisRevisionPage items(List<HypothesisRevision> items);

  HypothesisRevisionPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HypothesisRevisionPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HypothesisRevisionPage(...).copyWith(id: 12, name: "My name")
  /// ```
  HypothesisRevisionPage call({
    List<HypothesisRevision> items,
    String? nextCursor,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfHypothesisRevisionPage.copyWith(...)` or call `instanceOfHypothesisRevisionPage.copyWith.fieldName(value)` for a single field.
class _$HypothesisRevisionPageCWProxyImpl
    implements _$HypothesisRevisionPageCWProxy {
  const _$HypothesisRevisionPageCWProxyImpl(this._value);

  final HypothesisRevisionPage _value;

  @override
  HypothesisRevisionPage items(List<HypothesisRevision> items) =>
      call(items: items);

  @override
  HypothesisRevisionPage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HypothesisRevisionPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HypothesisRevisionPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  HypothesisRevisionPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return HypothesisRevisionPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<HypothesisRevision>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $HypothesisRevisionPageCopyWith on HypothesisRevisionPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfHypothesisRevisionPage.copyWith(...)` or `instanceOfHypothesisRevisionPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$HypothesisRevisionPageCWProxy get copyWith =>
      _$HypothesisRevisionPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

HypothesisRevisionPage _$HypothesisRevisionPageFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('HypothesisRevisionPage', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['items']);
  final val = HypothesisRevisionPage(
    items: $checkedConvert(
      'items',
      (v) => (v as List<dynamic>)
          .map((e) => HypothesisRevision.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$HypothesisRevisionPageToJson(
  HypothesisRevisionPage instance,
) => <String, dynamic>{
  'items': instance.items.map((e) => e.toJson()).toList(),
  'nextCursor': ?instance.nextCursor,
};
