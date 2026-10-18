// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_activate_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleActivateRequestCWProxy {
  ChronicleActivateRequest backfill(ChronicleActivateRequestBackfill? backfill);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleActivateRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleActivateRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleActivateRequest call({ChronicleActivateRequestBackfill? backfill});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleActivateRequest.copyWith(...)` or call `instanceOfChronicleActivateRequest.copyWith.fieldName(value)` for a single field.
class _$ChronicleActivateRequestCWProxyImpl
    implements _$ChronicleActivateRequestCWProxy {
  const _$ChronicleActivateRequestCWProxyImpl(this._value);

  final ChronicleActivateRequest _value;

  @override
  ChronicleActivateRequest backfill(
    ChronicleActivateRequestBackfill? backfill,
  ) => call(backfill: backfill);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleActivateRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleActivateRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleActivateRequest call({
    Object? backfill = const $CopyWithPlaceholder(),
  }) {
    return ChronicleActivateRequest(
      backfill: backfill == const $CopyWithPlaceholder()
          ? _value.backfill
          // ignore: cast_nullable_to_non_nullable
          : backfill as ChronicleActivateRequestBackfill?,
    );
  }
}

extension $ChronicleActivateRequestCopyWith on ChronicleActivateRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleActivateRequest.copyWith(...)` or `instanceOfChronicleActivateRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleActivateRequestCWProxy get copyWith =>
      _$ChronicleActivateRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleActivateRequest _$ChronicleActivateRequestFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ChronicleActivateRequest', json, ($checkedConvert) {
  final val = ChronicleActivateRequest(
    backfill: $checkedConvert(
      'backfill',
      (v) => v == null
          ? null
          : ChronicleActivateRequestBackfill.fromJson(
              v as Map<String, dynamic>,
            ),
    ),
  );
  return val;
});

Map<String, dynamic> _$ChronicleActivateRequestToJson(
  ChronicleActivateRequest instance,
) => <String, dynamic>{'backfill': ?instance.backfill?.toJson()};
