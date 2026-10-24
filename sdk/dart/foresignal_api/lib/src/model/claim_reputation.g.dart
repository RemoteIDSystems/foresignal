// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'claim_reputation.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ClaimReputationCWProxy {
  ClaimReputation outletId(String outletId);

  ClaimReputation factuality(String factuality);

  ClaimReputation lean(String? lean);

  ClaimReputation stateAffiliated(bool stateAffiliated);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ClaimReputation(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ClaimReputation(...).copyWith(id: 12, name: "My name")
  /// ```
  ClaimReputation call({
    String outletId,
    String factuality,
    String? lean,
    bool stateAffiliated,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfClaimReputation.copyWith(...)` or call `instanceOfClaimReputation.copyWith.fieldName(value)` for a single field.
class _$ClaimReputationCWProxyImpl implements _$ClaimReputationCWProxy {
  const _$ClaimReputationCWProxyImpl(this._value);

  final ClaimReputation _value;

  @override
  ClaimReputation outletId(String outletId) => call(outletId: outletId);

  @override
  ClaimReputation factuality(String factuality) => call(factuality: factuality);

  @override
  ClaimReputation lean(String? lean) => call(lean: lean);

  @override
  ClaimReputation stateAffiliated(bool stateAffiliated) =>
      call(stateAffiliated: stateAffiliated);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ClaimReputation(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ClaimReputation(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ClaimReputation call({
    Object? outletId = const $CopyWithPlaceholder(),
    Object? factuality = const $CopyWithPlaceholder(),
    Object? lean = const $CopyWithPlaceholder(),
    Object? stateAffiliated = const $CopyWithPlaceholder(),
  }) {
    return ClaimReputation(
      outletId: outletId == const $CopyWithPlaceholder() || outletId == null
          ? _value.outletId
          // ignore: cast_nullable_to_non_nullable
          : outletId as String,
      factuality:
          factuality == const $CopyWithPlaceholder() || factuality == null
          ? _value.factuality
          // ignore: cast_nullable_to_non_nullable
          : factuality as String,
      lean: lean == const $CopyWithPlaceholder()
          ? _value.lean
          // ignore: cast_nullable_to_non_nullable
          : lean as String?,
      stateAffiliated:
          stateAffiliated == const $CopyWithPlaceholder() ||
              stateAffiliated == null
          ? _value.stateAffiliated
          // ignore: cast_nullable_to_non_nullable
          : stateAffiliated as bool,
    );
  }
}

extension $ClaimReputationCopyWith on ClaimReputation {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfClaimReputation.copyWith(...)` or `instanceOfClaimReputation.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ClaimReputationCWProxy get copyWith => _$ClaimReputationCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ClaimReputation _$ClaimReputationFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ClaimReputation', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['outletId', 'factuality', 'stateAffiliated'],
      );
      final val = ClaimReputation(
        outletId: $checkedConvert('outletId', (v) => v as String),
        factuality: $checkedConvert('factuality', (v) => v as String),
        lean: $checkedConvert('lean', (v) => v as String?),
        stateAffiliated: $checkedConvert('stateAffiliated', (v) => v as bool),
      );
      return val;
    });

Map<String, dynamic> _$ClaimReputationToJson(ClaimReputation instance) =>
    <String, dynamic>{
      'outletId': instance.outletId,
      'factuality': instance.factuality,
      'lean': ?instance.lean,
      'stateAffiliated': instance.stateAffiliated,
    };
