// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'claim_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ClaimPageCWProxy {
  ClaimPage items(List<Claim> items);

  ClaimPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ClaimPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ClaimPage(...).copyWith(id: 12, name: "My name")
  /// ```
  ClaimPage call({List<Claim> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfClaimPage.copyWith(...)` or call `instanceOfClaimPage.copyWith.fieldName(value)` for a single field.
class _$ClaimPageCWProxyImpl implements _$ClaimPageCWProxy {
  const _$ClaimPageCWProxyImpl(this._value);

  final ClaimPage _value;

  @override
  ClaimPage items(List<Claim> items) => call(items: items);

  @override
  ClaimPage nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ClaimPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ClaimPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ClaimPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return ClaimPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<Claim>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $ClaimPageCopyWith on ClaimPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfClaimPage.copyWith(...)` or `instanceOfClaimPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ClaimPageCWProxy get copyWith => _$ClaimPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ClaimPage _$ClaimPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ClaimPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items', 'nextCursor']);
      final val = ClaimPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => Claim.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$ClaimPageToJson(ClaimPage instance) => <String, dynamic>{
  'items': instance.items.map((e) => e.toJson()).toList(),
  'nextCursor': instance.nextCursor,
};
