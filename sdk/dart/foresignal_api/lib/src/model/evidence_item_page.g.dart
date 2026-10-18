// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'evidence_item_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EvidenceItemPageCWProxy {
  EvidenceItemPage items(List<EvidenceItem> items);

  EvidenceItemPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EvidenceItemPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EvidenceItemPage(...).copyWith(id: 12, name: "My name")
  /// ```
  EvidenceItemPage call({List<EvidenceItem> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEvidenceItemPage.copyWith(...)` or call `instanceOfEvidenceItemPage.copyWith.fieldName(value)` for a single field.
class _$EvidenceItemPageCWProxyImpl implements _$EvidenceItemPageCWProxy {
  const _$EvidenceItemPageCWProxyImpl(this._value);

  final EvidenceItemPage _value;

  @override
  EvidenceItemPage items(List<EvidenceItem> items) => call(items: items);

  @override
  EvidenceItemPage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EvidenceItemPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EvidenceItemPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EvidenceItemPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return EvidenceItemPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<EvidenceItem>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $EvidenceItemPageCopyWith on EvidenceItemPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEvidenceItemPage.copyWith(...)` or `instanceOfEvidenceItemPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EvidenceItemPageCWProxy get copyWith => _$EvidenceItemPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EvidenceItemPage _$EvidenceItemPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('EvidenceItemPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items', 'nextCursor']);
      final val = EvidenceItemPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => EvidenceItem.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$EvidenceItemPageToJson(EvidenceItemPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': instance.nextCursor,
    };
