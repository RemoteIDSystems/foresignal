// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'stream_ticket.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$StreamTicketCWProxy {
  StreamTicket ticket(String ticket);

  StreamTicket purpose(StreamTicketPurposeEnum purpose);

  StreamTicket expiresAt(DateTime expiresAt);

  StreamTicket paths(List<String>? paths);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StreamTicket(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StreamTicket(...).copyWith(id: 12, name: "My name")
  /// ```
  StreamTicket call({
    String ticket,
    StreamTicketPurposeEnum purpose,
    DateTime expiresAt,
    List<String>? paths,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfStreamTicket.copyWith(...)` or call `instanceOfStreamTicket.copyWith.fieldName(value)` for a single field.
class _$StreamTicketCWProxyImpl implements _$StreamTicketCWProxy {
  const _$StreamTicketCWProxyImpl(this._value);

  final StreamTicket _value;

  @override
  StreamTicket ticket(String ticket) => call(ticket: ticket);

  @override
  StreamTicket purpose(StreamTicketPurposeEnum purpose) =>
      call(purpose: purpose);

  @override
  StreamTicket expiresAt(DateTime expiresAt) => call(expiresAt: expiresAt);

  @override
  StreamTicket paths(List<String>? paths) => call(paths: paths);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StreamTicket(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StreamTicket(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  StreamTicket call({
    Object? ticket = const $CopyWithPlaceholder(),
    Object? purpose = const $CopyWithPlaceholder(),
    Object? expiresAt = const $CopyWithPlaceholder(),
    Object? paths = const $CopyWithPlaceholder(),
  }) {
    return StreamTicket(
      ticket: ticket == const $CopyWithPlaceholder() || ticket == null
          ? _value.ticket
          // ignore: cast_nullable_to_non_nullable
          : ticket as String,
      purpose: purpose == const $CopyWithPlaceholder() || purpose == null
          ? _value.purpose
          // ignore: cast_nullable_to_non_nullable
          : purpose as StreamTicketPurposeEnum,
      expiresAt: expiresAt == const $CopyWithPlaceholder() || expiresAt == null
          ? _value.expiresAt
          // ignore: cast_nullable_to_non_nullable
          : expiresAt as DateTime,
      paths: paths == const $CopyWithPlaceholder()
          ? _value.paths
          // ignore: cast_nullable_to_non_nullable
          : paths as List<String>?,
    );
  }
}

extension $StreamTicketCopyWith on StreamTicket {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfStreamTicket.copyWith(...)` or `instanceOfStreamTicket.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$StreamTicketCWProxy get copyWith => _$StreamTicketCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

StreamTicket _$StreamTicketFromJson(Map<String, dynamic> json) =>
    $checkedCreate('StreamTicket', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['ticket', 'purpose', 'expiresAt']);
      final val = StreamTicket(
        ticket: $checkedConvert('ticket', (v) => v as String),
        purpose: $checkedConvert(
          'purpose',
          (v) => $enumDecode(_$StreamTicketPurposeEnumEnumMap, v),
        ),
        expiresAt: $checkedConvert(
          'expiresAt',
          (v) => DateTime.parse(v as String),
        ),
        paths: $checkedConvert(
          'paths',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$StreamTicketToJson(StreamTicket instance) =>
    <String, dynamic>{
      'ticket': instance.ticket,
      'purpose': _$StreamTicketPurposeEnumEnumMap[instance.purpose]!,
      'expiresAt': instance.expiresAt.toIso8601String(),
      'paths': ?instance.paths,
    };

const _$StreamTicketPurposeEnumEnumMap = {
  StreamTicketPurposeEnum.stream: 'stream',
  StreamTicketPurposeEnum.tiles: 'tiles',
};
