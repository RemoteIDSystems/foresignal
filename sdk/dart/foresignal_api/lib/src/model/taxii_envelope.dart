//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'taxii_envelope.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class TaxiiEnvelope {
  /// Returns a new [TaxiiEnvelope] instance.
  TaxiiEnvelope({

     this.more,

     this.next,

     this.objects,
  });

  @JsonKey(
    
    name: r'more',
    required: false,
    includeIfNull: false,
  )


  final bool? more;



  @JsonKey(
    
    name: r'next',
    required: false,
    includeIfNull: false,
  )


  final String? next;



  @JsonKey(
    
    name: r'objects',
    required: false,
    includeIfNull: false,
  )


  final List<Map<String, Object>>? objects;





    @override
    bool operator ==(Object other) => identical(this, other) || other is TaxiiEnvelope &&
      other.more == more &&
      other.next == next &&
      other.objects == objects;

    @override
    int get hashCode =>
        more.hashCode +
        (next == null ? 0 : next.hashCode) +
        objects.hashCode;

  factory TaxiiEnvelope.fromJson(Map<String, dynamic> json) => _$TaxiiEnvelopeFromJson(json);

  Map<String, dynamic> toJson() => _$TaxiiEnvelopeToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

