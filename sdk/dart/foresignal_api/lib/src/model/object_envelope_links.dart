//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'object_envelope_links.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ObjectEnvelopeLinks {
  /// Returns a new [ObjectEnvelopeLinks] instance.
  ObjectEnvelopeLinks({

     this.self,

     this.related,
  });

  @JsonKey(
    
    name: r'self',
    required: false,
    includeIfNull: false,
  )


  final String? self;



  @JsonKey(
    
    name: r'related',
    required: false,
    includeIfNull: false,
  )


  final String? related;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ObjectEnvelopeLinks &&
      other.self == self &&
      other.related == related;

    @override
    int get hashCode =>
        self.hashCode +
        related.hashCode;

  factory ObjectEnvelopeLinks.fromJson(Map<String, dynamic> json) => _$ObjectEnvelopeLinksFromJson(json);

  Map<String, dynamic> toJson() => _$ObjectEnvelopeLinksToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

