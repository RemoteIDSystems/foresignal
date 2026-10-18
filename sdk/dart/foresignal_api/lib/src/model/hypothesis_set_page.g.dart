// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'hypothesis_set_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$HypothesisSetPageCWProxy {
  HypothesisSetPage items(List<HypothesisSet> items);

  HypothesisSetPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HypothesisSetPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HypothesisSetPage(...).copyWith(id: 12, name: "My name")
  /// ```
  HypothesisSetPage call({List<HypothesisSet> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfHypothesisSetPage.copyWith(...)` or call `instanceOfHypothesisSetPage.copyWith.fieldName(value)` for a single field.
class _$HypothesisSetPageCWProxyImpl implements _$HypothesisSetPageCWProxy {
  const _$HypothesisSetPageCWProxyImpl(this._value);

  final HypothesisSetPage _value;

  @override
  HypothesisSetPage items(List<HypothesisSet> items) => call(items: items);

  @override
  HypothesisSetPage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HypothesisSetPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HypothesisSetPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  HypothesisSetPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return HypothesisSetPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<HypothesisSet>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $HypothesisSetPageCopyWith on HypothesisSetPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfHypothesisSetPage.copyWith(...)` or `instanceOfHypothesisSetPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$HypothesisSetPageCWProxy get copyWith =>
      _$HypothesisSetPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

HypothesisSetPage _$HypothesisSetPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('HypothesisSetPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = HypothesisSetPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => HypothesisSet.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$HypothesisSetPageToJson(HypothesisSetPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
    };
