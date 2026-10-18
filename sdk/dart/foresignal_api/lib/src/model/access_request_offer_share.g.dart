// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'access_request_offer_share.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AccessRequestOfferShareCWProxy {
  AccessRequestOfferShare chronicleId(String chronicleId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AccessRequestOfferShare(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AccessRequestOfferShare(...).copyWith(id: 12, name: "My name")
  /// ```
  AccessRequestOfferShare call({String chronicleId});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAccessRequestOfferShare.copyWith(...)` or call `instanceOfAccessRequestOfferShare.copyWith.fieldName(value)` for a single field.
class _$AccessRequestOfferShareCWProxyImpl
    implements _$AccessRequestOfferShareCWProxy {
  const _$AccessRequestOfferShareCWProxyImpl(this._value);

  final AccessRequestOfferShare _value;

  @override
  AccessRequestOfferShare chronicleId(String chronicleId) =>
      call(chronicleId: chronicleId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AccessRequestOfferShare(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AccessRequestOfferShare(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AccessRequestOfferShare call({
    Object? chronicleId = const $CopyWithPlaceholder(),
  }) {
    return AccessRequestOfferShare(
      chronicleId:
          chronicleId == const $CopyWithPlaceholder() || chronicleId == null
          ? _value.chronicleId
          // ignore: cast_nullable_to_non_nullable
          : chronicleId as String,
    );
  }
}

extension $AccessRequestOfferShareCopyWith on AccessRequestOfferShare {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAccessRequestOfferShare.copyWith(...)` or `instanceOfAccessRequestOfferShare.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AccessRequestOfferShareCWProxy get copyWith =>
      _$AccessRequestOfferShareCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AccessRequestOfferShare _$AccessRequestOfferShareFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AccessRequestOfferShare', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['chronicleId']);
  final val = AccessRequestOfferShare(
    chronicleId: $checkedConvert('chronicleId', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$AccessRequestOfferShareToJson(
  AccessRequestOfferShare instance,
) => <String, dynamic>{'chronicleId': instance.chronicleId};
