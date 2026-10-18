// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'stream_ticket_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$StreamTicketRequestCWProxy {
  StreamTicketRequest purpose(StreamTicketRequestPurposeEnum purpose);

  StreamTicketRequest paths(List<String>? paths);

  StreamTicketRequest ttl(String? ttl);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StreamTicketRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StreamTicketRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  StreamTicketRequest call({
    StreamTicketRequestPurposeEnum purpose,
    List<String>? paths,
    String? ttl,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfStreamTicketRequest.copyWith(...)` or call `instanceOfStreamTicketRequest.copyWith.fieldName(value)` for a single field.
class _$StreamTicketRequestCWProxyImpl implements _$StreamTicketRequestCWProxy {
  const _$StreamTicketRequestCWProxyImpl(this._value);

  final StreamTicketRequest _value;

  @override
  StreamTicketRequest purpose(StreamTicketRequestPurposeEnum purpose) =>
      call(purpose: purpose);

  @override
  StreamTicketRequest paths(List<String>? paths) => call(paths: paths);

  @override
  StreamTicketRequest ttl(String? ttl) => call(ttl: ttl);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StreamTicketRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StreamTicketRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  StreamTicketRequest call({
    Object? purpose = const $CopyWithPlaceholder(),
    Object? paths = const $CopyWithPlaceholder(),
    Object? ttl = const $CopyWithPlaceholder(),
  }) {
    return StreamTicketRequest(
      purpose: purpose == const $CopyWithPlaceholder() || purpose == null
          ? _value.purpose
          // ignore: cast_nullable_to_non_nullable
          : purpose as StreamTicketRequestPurposeEnum,
      paths: paths == const $CopyWithPlaceholder()
          ? _value.paths
          // ignore: cast_nullable_to_non_nullable
          : paths as List<String>?,
      ttl: ttl == const $CopyWithPlaceholder()
          ? _value.ttl
          // ignore: cast_nullable_to_non_nullable
          : ttl as String?,
    );
  }
}

extension $StreamTicketRequestCopyWith on StreamTicketRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfStreamTicketRequest.copyWith(...)` or `instanceOfStreamTicketRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$StreamTicketRequestCWProxy get copyWith =>
      _$StreamTicketRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

StreamTicketRequest _$StreamTicketRequestFromJson(Map<String, dynamic> json) =>
    $checkedCreate('StreamTicketRequest', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['purpose']);
      final val = StreamTicketRequest(
        purpose: $checkedConvert(
          'purpose',
          (v) => $enumDecode(_$StreamTicketRequestPurposeEnumEnumMap, v),
        ),
        paths: $checkedConvert(
          'paths',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        ttl: $checkedConvert('ttl', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$StreamTicketRequestToJson(
  StreamTicketRequest instance,
) => <String, dynamic>{
  'purpose': _$StreamTicketRequestPurposeEnumEnumMap[instance.purpose]!,
  'paths': ?instance.paths,
  'ttl': ?instance.ttl,
};

const _$StreamTicketRequestPurposeEnumEnumMap = {
  StreamTicketRequestPurposeEnum.stream: 'stream',
  StreamTicketRequestPurposeEnum.tiles: 'tiles',
};
