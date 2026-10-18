// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'trip_check_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TripCheckRequestCWProxy {
  TripCheckRequest dryRun(bool? dryRun);

  TripCheckRequest occurrence(DateTime? occurrence);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TripCheckRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TripCheckRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  TripCheckRequest call({bool? dryRun, DateTime? occurrence});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTripCheckRequest.copyWith(...)` or call `instanceOfTripCheckRequest.copyWith.fieldName(value)` for a single field.
class _$TripCheckRequestCWProxyImpl implements _$TripCheckRequestCWProxy {
  const _$TripCheckRequestCWProxyImpl(this._value);

  final TripCheckRequest _value;

  @override
  TripCheckRequest dryRun(bool? dryRun) => call(dryRun: dryRun);

  @override
  TripCheckRequest occurrence(DateTime? occurrence) =>
      call(occurrence: occurrence);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TripCheckRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TripCheckRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TripCheckRequest call({
    Object? dryRun = const $CopyWithPlaceholder(),
    Object? occurrence = const $CopyWithPlaceholder(),
  }) {
    return TripCheckRequest(
      dryRun: dryRun == const $CopyWithPlaceholder()
          ? _value.dryRun
          // ignore: cast_nullable_to_non_nullable
          : dryRun as bool?,
      occurrence: occurrence == const $CopyWithPlaceholder()
          ? _value.occurrence
          // ignore: cast_nullable_to_non_nullable
          : occurrence as DateTime?,
    );
  }
}

extension $TripCheckRequestCopyWith on TripCheckRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTripCheckRequest.copyWith(...)` or `instanceOfTripCheckRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TripCheckRequestCWProxy get copyWith => _$TripCheckRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TripCheckRequest _$TripCheckRequestFromJson(Map<String, dynamic> json) =>
    $checkedCreate('TripCheckRequest', json, ($checkedConvert) {
      final val = TripCheckRequest(
        dryRun: $checkedConvert('dryRun', (v) => v as bool? ?? false),
        occurrence: $checkedConvert(
          'occurrence',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$TripCheckRequestToJson(TripCheckRequest instance) =>
    <String, dynamic>{
      'dryRun': ?instance.dryRun,
      'occurrence': ?instance.occurrence?.toIso8601String(),
    };
